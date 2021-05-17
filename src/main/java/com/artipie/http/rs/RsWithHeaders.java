/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
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
