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
package com.artipie.http.headers;

import com.artipie.http.Headers;
import com.artipie.http.rq.RqHeaders;
import org.cactoos.text.Base64Encoded;

/**
 * Authorization header.
 *
 * @since 0.12
 * @todo #191:60min Extract header value parsing logic from `BasicIdentities`
 *  Now there is code for parsing `Authorization` header content,
 *  but it is contained in `BasicIdentities` class. The code could be generalized and extracted to
 *  `Authorization`, `Authorization.Bearer` and `BasicAuthorizationHeader` classes.
 */
public final class Authorization extends Header.Wrap {

    /**
     * Header name.
     */
    public static final String NAME = "Authorization";

    /**
     * Ctor.
     *
     * @param value Header value.
     */
    public Authorization(final String value) {
        super(new Header(Authorization.NAME, value));
    }

    /**
     * Ctor.
     *
     * @param headers Headers to extract header from.
     */
    public Authorization(final Headers headers) {
        this(new RqHeaders.Single(headers, Authorization.NAME).asString());
    }

    /**
     * Basic authentication `Authorization` header.
     *
     * @since 0.12
     */
    public static final class Basic extends Header.Wrap {

        /**
         * Ctor.
         *
         * @param username User name.
         * @param password Password.
         */
        public Basic(final String username, final String password) {
            super(
                new Authorization(
                    String.format(
                        "Basic %s",
                        new Base64Encoded(String.format("%s:%s", username, password))
                    )
                )
            );
        }
    }

    /**
     * Bearer authentication `Authorization` header.
     *
     * @since 0.12
     */
    public static final class Bearer extends Header.Wrap {

        /**
         * Ctor.
         *
         * @param token Token.
         */
        public Bearer(final String token) {
            super(new Authorization(String.format("Bearer %s", token)));
        }
    }
}
