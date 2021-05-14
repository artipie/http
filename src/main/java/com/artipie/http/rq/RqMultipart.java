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
import com.artipie.http.headers.ContentType;
import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * Multipart request.
 * <p>
 * Parses multipart request into body parts as publisher of {@link Request}s.
 * It accepts bodies acoording to
 * <a href="https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html">RFC-1341-7.2</a>
 * specification.
 * </p>
 *
 * @implNote Since the multipart body is always received sequentially part by part,
 *  the parts() method does not publish the next part until the previous is fully read.
 * @implNote The implementation does not keep request part data in memory or storage,
 *  it should process each chunk and send to proper downstream.
 * @implNote The body part will not be parsed until {@code parts()} method call.
 * @since 1.0
 */
public final class RqMultipart {

    /**
     * Content type.
     */
    private ContentType ctype;

    /**
     * Body upstream.
     */
    private Publisher<ByteBuffer> upstream;

    /**
     * Multipart request from headers and body upstream.
     * @param headers Request headers
     * @param body Upstream
     */
    public RqMultipart(final Headers headers, final Publisher<ByteBuffer> body) {
        this(new ContentType(headers), body);
    }

    /**
     * Multipart request from content type and body upstream.
     * @param ctype Content type
     * @param body Upstream
     */
    public RqMultipart(final ContentType ctype, final Publisher<ByteBuffer> body) {
        this.ctype = ctype;
        this.upstream = body;
    }

    /**
     * Body parts.
     * @return Publisher of parts
     */
    public Publisher<Part> parts() {
        throw new IllegalStateException(
            String.format("Not implemented (%s, %s)", this.ctype, this.upstream)
        );
    }

    /**
     * Part of multipart.
     * @since 1.0
     */
    public final class Part implements Publisher<ByteBuffer> {

        /**
         * Part headers.
         */
        private final Headers hdr;

        /**
         * Part body.
         */
        private final Publisher<ByteBuffer> source;

        /**
         * New part.
         * @param headers Part headers
         * @param source Part body
         */
        private Part(final Headers headers, final Publisher<ByteBuffer> source) {
            this.hdr = headers;
            this.source = source;
        }

        @Override
        public void subscribe(final Subscriber<? super ByteBuffer> sub) {
            this.source.subscribe(sub);
        }

        /**
         * Part headers.
         * @return Headers
         */
        public Headers headers() {
            return this.hdr;
        }
    }
}
