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

package com.artipie.http.tk;

import com.artipie.http.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;

/**
 * Takes response wrapper.
 * @since 0.1
 * @todo #5:30min Write a unit test for this class.
 *  It should verify:
 *  1) status line was added to response head
 *  2) all headers were joined and added to head
 *  3) body was converted from bytes flow to input stream
 */
public final class TkResponse implements org.takes.Response {

    /**
     * Artipie response.
     */
    private final Response rsp;

    /**
     * New Takes wrapper for Artipie response.
     * @param rsp Artipie response
     */
    public TkResponse(final Response rsp) {
        this.rsp = rsp;
    }

    @Override
    public Iterable<String> head() throws IOException {
        return new Joined<String>(
            this.rsp.status(),
            new Joined<>(
                new Mapped<>(
                    entry -> new Mapped<>(
                        value -> String.format("%s: %s", entry.getKey(), value),
                        new Joined<>(entry.getValue())
                    ),
                    this.rsp.headers().entrySet()
                )
            )
        );
    }

    @Override
    public InputStream body() throws IOException {
        final PipedInputStream input = new PipedInputStream();
        this.rsp.body().subscribe(new TkResponse.ToStreamSubscriber(new PipedOutputStream(input)));
        return input;
    }

    /**
     * Output stream as subscriber.
     * @since 0.1
     */
    private static final class ToStreamSubscriber implements Flow.Subscriber<Byte> {

        /**
         * Output stream.
         */
        private final OutputStream out;

        /**
         * Subscribe flow of bytes into output stream.
         * @param out Output stream
         */
        ToStreamSubscriber(final OutputStream out) {
            this.out = out;
        }

        @Override
        public void onSubscribe(final Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(final Byte item) {
            try {
                this.out.write(item);
            } catch (final IOException err) {
                throw new UncheckedIOException(err);
            }
        }

        @Override
        public void onError(final Throwable err) {
            try {
                this.out.close();
            } catch (final IOException iox) {
                throw new UncheckedIOException(iox);
            }
            throw new IllegalStateException(err);
        }

        @Override
        public void onComplete() {
            try {
                this.out.close();
            } catch (final IOException err) {
                throw new UncheckedIOException(err);
            }
        }
    }
}
