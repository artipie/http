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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Ensure that {@link RequestLine} works correctly.
 *
 * @since 0.1
 */
public class RequestLineTest {

    @Test
    public void reqLineStringIsCorrect() {
        MatcherAssert.assertThat(
            new RequestLine("GET", "/pub/WWW/TheProject.html", "HTTP/1.1").toString(),
            Matchers.equalTo("GET /pub/WWW/TheProject.html HTTP/1.1\r\n")
        );
    }

    @Test
    public void shouldHaveDefaultVersionWhenNoneSpecified() {
        MatcherAssert.assertThat(
            new RequestLine(RqMethod.PUT, "/file.txt").toString(),
            Matchers.equalTo("PUT /file.txt HTTP/1.1\r\n")
        );
    }
}
