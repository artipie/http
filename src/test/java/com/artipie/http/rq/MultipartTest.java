/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.http.rq;

import com.artipie.http.Headers;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.stream.ByteFlowAsString;
import com.artipie.vertx.VertxSliceServer;
import io.reactivex.Flowable;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.multipart.MultipartForm;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Multipart parser test.
 * @since 0.4
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class MultipartTest {

    /**
     * The localhost.
     */
    private static final String LOCALHOST = "localhost";

    @Test
    public void ableToParseBasic(@TempDir final Path dir) throws IOException {
        final Vertx vertx = Vertx.vertx();
        final int port = this.rndPort();
        final VertxSliceServer server = new VertxSliceServer(
            vertx,
            (line, headers, body) -> connection -> {
                new Thread(
                    () -> {
                        final Multipart mpp = new Multipart(headers);
                        body.subscribe(mpp);
                        MatcherAssert.assertThat(
                            new ByteFlowAsString(
                                Flowable.fromPublisher(mpp).flatMap(part -> part)
                            ).value(),
                            new IsEqual<>("Hello worrrrld!!!\r\nHello worrrrld!!!\r\n")
                        );
                        connection.accept(
                            RsStatus.OK,
                            Headers.EMPTY,
                            Flowable.empty()
                        );
                    }).start();
                return CompletableFuture.completedFuture(null);
            },
            port
        );
        server.start();
        final Path resolve = dir.resolve("text.txt");
        Files.write(resolve, "Hello worrrrld!!!".getBytes());
        WebClient.create(vertx).post(port, MultipartTest.LOCALHOST, "/hello")
            .rxSendMultipartForm(
                MultipartForm.create()
                    .textFileUpload(
                        "hello",
                        resolve.getFileName().toString(),
                        resolve.toAbsolutePath().toString(),
                        "text/plain"
                    )
                    .textFileUpload(
                        "hello2",
                        resolve.getFileName().toString(),
                        resolve.toAbsolutePath().toString(),
                        "text/plain"
                    )
            ).blockingGet();
        server.stop();
        vertx.close();
    }

    /**
     * Find a random port.
     *
     * @return The free port.
     * @throws IOException If fails.
     */
    private int rndPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
