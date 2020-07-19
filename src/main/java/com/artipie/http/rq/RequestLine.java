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

/**
 * Http Request Line.
 * <p>
 * See: 5.1 https://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html
 * @since 0.1
 */
public final class RequestLine {

    /**
     * The request method.
     */
    private final String method;

    /**
     * The request uri.
     */
    private final String uri;

    /**
     * The Http version.
     */
    private final String version;

    /**
     * Ctor.
     *
     * @param method Request method.
     * @param uri Request URI.
     */
    public RequestLine(final RqMethod method, final String uri) {
        this(method.value(), uri);
    }

    /**
     * Ctor.
     *
     * @param method Request method.
     * @param uri Request URI.
     */
    public RequestLine(final String method, final String uri) {
        this(method, uri, "HTTP/1.1");
    }

    /**
     * Ctor.
     * @param method The http method.
     * @param uri The http uri.
     * @param version The http version.
     */
    public RequestLine(final String method, final String uri, final String version) {
        this.method = method;
        this.uri = uri;
        this.version = version;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s\r\n", this.method, this.uri, this.version);
    }
}
