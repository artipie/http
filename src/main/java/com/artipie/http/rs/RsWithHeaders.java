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

package com.artipie.http.rs;

import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Response with additional headers.
 *
 * @since 0.7
 */
public final class RsWithHeaders implements Response {

    /**
     * Origin response.
     */
    private final Response origin;

    /**
     * Headers.
     */
    private final Headers headers;

    /**
     * Ctor.
     *
     * @param origin Response
     * @param headers Headers
     */
    public RsWithHeaders(final Response origin, final Headers headers) {
        this.origin = origin;
        this.headers = headers;
    }

    /**
     * Ctor.
     *
     * @param origin Origin response.
     * @param headers Headers
     */
    public RsWithHeaders(final Response origin, final Iterable<Map.Entry<String, String>> headers) {
        this(origin, new Headers.From(headers));
    }

    /**
     * Ctor.
     *
     * @param origin Origin response.
     * @param headers Headers
     */
    @SafeVarargs
    public RsWithHeaders(final Response origin, final Map.Entry<String, String>... headers) {
        this(origin, new Headers.From(headers));
    }

    /**
     * Ctor.
     *
     * @param origin Origin response.
     * @param name Name of header.
     * @param value Value of header.
     */
    public RsWithHeaders(final Response origin, final String name, final String value) {
        this(origin, new Headers.From(name, value));
    }

    @Override
    public CompletionStage<Void> send(final Connection con) {
        return this.origin.send(new RsWithHeaders.ConWithHeaders(con, this.headers));
    }

    /**
     * Connection with additional headers.
     * @since 0.3
     */
    private static final class ConWithHeaders implements Connection {

        /**
         * Origin connection.
         */
        private final Connection origin;

        /**
         * Additional headers.
         */
        private final Iterable<Map.Entry<String, String>> headers;

        /**
         * Ctor.
         *
         * @param origin Connection
         * @param headers Headers
         */
        private ConWithHeaders(
            final Connection origin,
            final Iterable<Map.Entry<String, String>> headers) {
            this.origin = origin;
            this.headers = headers;
        }

        @Override
        public CompletionStage<Void> accept(
            final RsStatus status,
            final Headers hrs,
            final Publisher<ByteBuffer> body
        ) {
            return this.origin.accept(
                status,
                new Headers.From(this.headers, hrs),
                body
            );
        }
    }
}
