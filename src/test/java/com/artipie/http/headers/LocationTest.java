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
 * Test case for {@link Location}.
 *
 * @since 0.11
 */
public final class LocationTest {

    @Test
    void shouldHaveExpectedName() {
        MatcherAssert.assertThat(
            new Location("http://artipie.com/").getKey(),
            new IsEqual<>("Location")
        );
    }

    @Test
    void shouldHaveExpectedValue() {
        final String value = "http://artipie.com/something";
        MatcherAssert.assertThat(
            new Location(value).getValue(),
            new IsEqual<>(value)
        );
    }

    @Test
    void shouldExtractValueFromHeaders() {
        final String value = "http://artipie.com/resource";
        final Location header = new Location(
            new Headers.From(
                new Header("Content-Length", "11"),
                new Header("location", value),
                new Header("X-Something", "Some Value")
            )
        );
        MatcherAssert.assertThat(header.getValue(), new IsEqual<>(value));
    }

    @Test
    void shouldFailToExtractValueFromEmptyHeaders() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new Location(Headers.EMPTY).getValue()
        );
    }

    @Test
    void shouldFailToExtractValueWhenNoLocationHeaders() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new Location(
                new Headers.From("Content-Type", "text/plain")
            ).getValue()
        );
    }

    @Test
    void shouldFailToExtractValueFromMultipleHeaders() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new Location(
                new Headers.From(
                    new Location("http://artipie.com/1"),
                    new Location("http://artipie.com/2")
                )
            ).getValue()
        );
    }
}
