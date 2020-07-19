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
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ContentLength}.
 *
 * @since 0.10
 */
public final class ContentLengthTest {

    @Test
    void shouldHaveExpectedValue() {
        MatcherAssert.assertThat(
            new ContentLength("10").getKey(),
            new IsEqual<>("Content-Length")
        );
    }

    @Test
    void shouldExtractLongValueFromHeaders() {
        final long length = 123;
        final ContentLength header = new ContentLength(
            new Headers.From(
                new Header("Content-Type", "application/octet-stream"),
                new Header("content-length", String.valueOf(length)),
                new Header("X-Something", "Some Value")
            )
        );
        MatcherAssert.assertThat(header.longValue(), new IsEqual<>(length));
    }

    @Test
    void shouldFailToExtractLongValueFromEmptyHeaders() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new ContentLength(Headers.EMPTY).longValue()
        );
    }
}
