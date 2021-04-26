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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Accept}.
 * @since 0.19
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class AcceptTest {

    @Test
    void parsesAndSortsHeaderValues() {
        MatcherAssert.assertThat(
            new Accept(
                new Headers.From(
                    new Header("Accept", "text/html, application/xml;q=0.9, audio/aac;q=0.4"),
                    new Header("Accept", "image/webp;q=0.5, multipart/mixed")
                )
            ).values(),
            Matchers.anyOf(
                Matchers.contains(
                    "text/html", "multipart/mixed", "application/xml", "image/webp", "audio/aac"
                ),
                Matchers.contains(
                    "multipart/mixed", "text/html", "application/xml", "image/webp", "audio/aac"
                )
            )
        );
    }

    @Test
    void parsesAndSortsRepeatingHeaderValues() {
        MatcherAssert.assertThat(
            new Accept(
                new Headers.From(
                    // @checkstyle LineLengthCheck (2 lines)
                    new Header("Accept", "text/html;q=0.6, application/xml;q=0.9, image/bmp;q=0.3"),
                    new Header("Accept", "image/bmp;q=0.5, text/html, multipart/mixed, text/json;q=0.4")
                )
            ).values(),
            Matchers.contains(
                "multipart/mixed", "application/xml", "text/html", "text/json", "image/bmp"
            )
        );
    }

    @Test
    void parsesOnlyAcceptHeader() {
        MatcherAssert.assertThat(
            new Accept(
                new Headers.From(
                    new Header("Accept", " audio/aac;q=0.4, application/json;q=0.9, text/*;q=0.1"),
                    new Header("Another header", "image/jpg")
                )
            ).values(),
            Matchers.contains("application/json", "audio/aac", "text/*")
        );
    }
}
