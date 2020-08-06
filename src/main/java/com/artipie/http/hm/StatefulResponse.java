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
package com.artipie.http.hm;

import com.artipie.asto.Concatenation;
import com.artipie.asto.Remaining;
import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.reactivestreams.Publisher;

/**
 * Response that keeps self state and can fetch it only once and reply many times.
 * <p>It can be useful when testing one response against multiple matchers, and response
 * from slice should be called only once.</p>
 * @since 0.16
 */
public final class StatefulResponse implements Response {

    /**
     * Origin response.
     */
    private final Response origin;

    /**
     * Stateful connection.
     */
    private final StatefulConnection con;

    /**
     * Wraps response with stateful connection.
     * @param origin Origin response
     */
    public StatefulResponse(final Response origin) {
        this.origin = origin;
        this.con = new StatefulConnection();
    }

    @Override
    public CompletionStage<Void> send(final Connection connection) {
        return this.con.load(this.origin).thenCompose(self -> self.replay(connection));
    }

    @Override
    public String toString() {
        return String.format(
            "(%s: state=%s)",
            this.getClass().getSimpleName(),
            this.con.toString()
        );
    }

    /**
     * Connection that keeps response state and can reply it to other connection.
     * @since 0.16
     */
    private static final class StatefulConnection implements Connection {

        /**
         * Response status.
         */
        private volatile RsStatus status;

        /**
         * Response headers.
         */
        private volatile Headers headers;

        /**
         * Response body.
         */
        private volatile Publisher<ByteBuffer> body;

        @Override
        public CompletionStage<Void> accept(final RsStatus stts, final Headers hdrs,
            final Publisher<ByteBuffer> bdy) {
            this.status = stts;
            this.headers = hdrs;
            this.body = Flowable.fromPublisher(bdy).cache();
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public String toString() {
            return String.format(
                "(%s: status=%s, headers=[%s], body=%s)",
                this.getClass().getSimpleName(),
                this.status,
                StreamSupport.stream(this.headers.spliterator(), false)
                    .map(
                        header -> String.format(
                            "\"%s\": \"%s\"",
                            header.getKey(),
                            header.getValue()
                        )
                    ).collect(Collectors.joining(", ")),
                Arrays.toString(
                    new Concatenation(this.body)
                        .single()
                        .map(buf -> new Remaining(buf).bytes())
                        .blockingGet()
                )
            );
        }

        /**
         * Load state from response if needed.
         * @param response Response to load the state
         * @return Self future
         */
        CompletionStage<StatefulConnection> load(final Response response) {
            final CompletionStage<StatefulConnection> self;
            if (this.status == null && this.headers == null && this.body == null) {
                self = response.send(this).thenApply(none -> this);
            } else {
                self = CompletableFuture.completedFuture(this);
            }
            return self;
        }

        /**
         * Reply self state to connection.
         * @param connection Connection
         * @return Future
         */
        CompletionStage<Void> replay(final Connection connection) {
            return connection.accept(this.status, this.headers, this.body);
        }
    }
}
