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
import java.util.Map;

/**
 * Path prefix obtained from X-FullPath header and request line.
 * @since 0.16
 */
public final class RequestLinePrefix {

    /**
     * Full path header name.
     */
    private static final String HDR_FULL_PATH = "X-FullPath";

    /**
     * Request line.
     */
    private final String line;

    /**
     * Headers.
     */
    private final Headers headers;

    /**
     * Ctor.
     * @param line Request line
     * @param headers Request headers
     */
    public RequestLinePrefix(final String line, final Headers headers) {
        this.line = line;
        this.headers = headers;
    }

    /**
     * Ctor.
     * @param line Request line
     * @param headers Request headers
     */
    public RequestLinePrefix(final String line, final Iterable<Map.Entry<String, String>> headers) {
        this(line, new Headers.From(headers));
    }

    /**
     * Obtains path prefix by `X-FullPath` header and request line. If header is absent, empty line
     * is returned.
     * @return Path prefix
     */
    public String get() {
        return new RqHeaders(this.headers, RequestLinePrefix.HDR_FULL_PATH).stream()
            .findFirst()
            .map(
                item -> {
                    final String res;
                    final String first = this.line.replaceAll("^/", "").replaceAll("/$", "")
                        .split("/")[0];
                    if (item.indexOf(first) > 0) {
                        res = item.substring(0, item.indexOf(first) - 1);
                    } else {
                        res = item;
                    }
                    return res;
                }
            ).orElse("");
    }
}
