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

package com.artipie.http;

import java.util.Map;
import java.util.concurrent.Flow;

/**
 * HTTP response.
 * See {@link https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html}.
 * @since 0.1
 */
public interface Response {

    /**
     * Status line.
     * <p>
     * Status line is {@code HTTP-Version SP Status-Code SP Reason-Phrase CRLF}.
     * See 6.1 section of rfc2616.
     * </p>
     * @return Status string
     */
    String status();

    /**
     * General headers, response headers, entity headers.
     * <p>
     * See 4.5, 6.2 and 7.1 section of rfc2616.
     * </p>
     * @return Headers map of values by name
     */
    Map<String, Iterable<String>> headers();

    /**
     * Response body flow.
     * <p>
     * See 7.2 section of rfc2616.
     * </p>
     * @return Bytes flow
     */
    Flow.Publisher<Byte> body();
}
