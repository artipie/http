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

import com.google.common.base.Splitter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * URI query parameters. See <a href="https://tools.ietf.org/html/rfc3986#section-3.4">RFC</a>.
 *
 * @since 0.18
 */
public final class RqParams {

    /**
     * Request query.
     */
    private final String query;

    /**
     * Ctor.
     *
     * @param uri Request URI.
     */
    public RqParams(final URI uri) {
        this(uri.getQuery());
    }

    /**
     * Ctor.
     *
     * @param query Request query.
     */
    public RqParams(final String query) {
        this.query = query;
    }

    /**
     * Get value for parameter value by name.
     * Empty {@link Optional} is returned if parameter not found.
     * First value is returned if multiple parameters with same name present in the query.
     *
     * @param name Parameter name.
     * @return Parameter value.
     */
    public Optional<String> value(final String name) {
        final Optional<String> result;
        if (this.query == null) {
            result = Optional.empty();
        } else {
            result = StreamSupport.stream(
                Splitter.on("&").omitEmptyStrings().split(this.query).spliterator(),
                false
            ).flatMap(
                param -> {
                    final String prefix = String.format("%s=", name);
                    final Stream<String> value;
                    if (param.startsWith(prefix)) {
                        value = Stream.of(param.substring(prefix.length()));
                    } else {
                        value = Stream.empty();
                    }
                    return value;
                }
            ).findFirst();
        }
        return result.map(RqParams::decode);
    }

    /**
     * Decode string using URL-encoding.
     * @param enc Encoded string
     * @return Decoded string
     */
    private static String decode(final String enc) {
        try {
            return URLDecoder.decode(enc, "UTF-8");
        } catch (final UnsupportedEncodingException err) {
            throw new IllegalStateException(err);
        }
    }
}
