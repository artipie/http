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

import com.artipie.http.headers.Header;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Headers.From}.
 *
 * @since 0.11
 */
class HeadersFromTest {

    @Test
    public void shouldConcatWithHeader() {
        final Header header = new Header("h1", "v1");
        final String name = "h2";
        final String value = "v2";
        MatcherAssert.assertThat(
            new Headers.From(new Headers.From(header), name, value),
            Matchers.contains(header, new Header(name, value))
        );
    }

    @Test
    public void shouldConcatWithHeaders() {
        final Header origin = new Header("hh1", "vv1");
        final Header one = new Header("hh2", "vv2");
        final Header two = new Header("hh3", "vv3");
        MatcherAssert.assertThat(
            new Headers.From(new Headers.From(origin), one, two),
            Matchers.contains(origin, one, two)
        );
    }
}
