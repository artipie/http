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
package com.artipie.http.slice;

import com.artipie.http.Headers;
import com.artipie.http.Slice;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import io.reactivex.Flowable;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import org.cactoos.map.MapEntry;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LoggingSlice}.
 *
 * @since 0.8
 */
class LoggingSliceTest {

    @Test
    void shouldLogRequestAndResponse() {
        new LoggingSlice(
            Level.INFO,
            new SliceSimple(
                new RsWithHeaders(
                    new RsWithStatus(RsStatus.OK),
                    "Request-Header", "some; value"
                )
            )
        ).response(
            "GET /v2/ HTTP_1_1",
            Arrays.asList(
                new MapEntry<>("Content-Length", "0"),
                new MapEntry<>("Content-Type", "whatever")
            ),
            Flowable.empty()
        ).send(
            (status, headers, body) -> CompletableFuture.allOf()
        ).toCompletableFuture().join();
    }

    @Test
    void shouldLogAndPreserveExceptionInSlice() {
        final IllegalStateException error = new IllegalStateException("Error in slice");
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                Throwable.class,
                () -> this.handle(
                    (line, headers, body) -> {
                        throw error;
                    }
                )
            ),
            new IsEqual<>(error)
        );
    }

    @Test
    void shouldLogAndPreserveExceptionInResponse() {
        final IllegalStateException error = new IllegalStateException("Error in response");
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                Throwable.class,
                () -> this.handle(
                    (line, headers, body) -> conn -> {
                        throw error;
                    }
                )
            ),
            new IsEqual<>(error)
        );
    }

    @Test
    void shouldLogAndPreserveAsyncExceptionInResponse() {
        final IllegalStateException error = new IllegalStateException("Error in response async");
        final CompletionStage<Void> result = this.handle(
            (line, headers, body) -> conn -> {
                final CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(error);
                return future;
            }
        );
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                Throwable.class,
                () -> result.toCompletableFuture().join()
            ).getCause(),
            new IsEqual<>(error)
        );
    }

    private CompletionStage<Void> handle(final Slice slice) {
        return new LoggingSlice(Level.INFO, slice).response(
            "GET /hello/ HTTP/1.1",
            Headers.EMPTY,
            Flowable.empty()
        ).send((status, headers, body) -> CompletableFuture.allOf());
    }
}
