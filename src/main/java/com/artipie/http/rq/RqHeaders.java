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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.cactoos.Text;
import org.cactoos.iterable.Filtered;
import org.cactoos.list.ListEnvelope;
import org.cactoos.list.Mapped;

/**
 * Request headers.
 * <p>
 * Request header values by name from headers.
 * Usage (assume {@link com.artipie.http.Slice} implementation):
 * <pre><code>
 *  Response response(String line, Iterable&lt;Map.Entry&lt;String, String&gt;&gt; headers,
 *      Flow.Publisher&lt;ByteBuffer&gt; body) {
 *          List&lt;String&gt; values = new RqHeaders(headers, "content-type");
 *          // use these headers
 *  }
 * </code></pre>
 * Header names are case-insensitive, according to
 * <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2">RFC2616 SPEC</a>:
 * <br>
 * &gt; Each header field consists of a name followed by a colon (":") and the field value.
 * <br>
 * &gt; Field names are case-insensitive
 * </p>
 * @since 0.4
 */
public final class RqHeaders extends ListEnvelope<String> implements List<String> {

    /**
     * Header values by name.
     * @param headers All headers
     * @param name Header name
     */
    public RqHeaders(final Iterable<Map.Entry<String, String>> headers, final String name) {
        super(
            new Mapped<>(
                Map.Entry::getValue,
                new Filtered<>(
                    entry -> Objects.equals(
                        entry.getKey().toLowerCase(Locale.US),
                        name.toLowerCase(Locale.US)
                    ),
                    headers
                )
            )
        );
    }

    /**
     * Single header by name.
     * <p>
     * Use this class to find single header value by name:
     * <pre><code>
     * Text header = new RqHeaders.Single(headers, "content-type");
     * </code></pre>
     * If no headers were found or headers contains more than one value
     * for name {@link IllegalStateException} will be thrown.
     * </p>
     * @since 0.4
     */
    public static final class Single implements Text {

        /**
         * All header values.
         */
        private final List<String> headers;

        /**
         * Single header value among other.
         * @param headers All header values
         * @param name Header name
         */
        public Single(final Iterable<Map.Entry<String, String>> headers, final String name) {
            this.headers = new RqHeaders(headers, name);
        }

        @Override
        public String asString() {
            if (this.headers.isEmpty()) {
                throw new IllegalStateException("No headers were found");
            }
            if (this.headers.size() > 1) {
                throw new IllegalStateException("Too many headers were found");
            }
            return this.headers.get(0);
        }
    }
}
