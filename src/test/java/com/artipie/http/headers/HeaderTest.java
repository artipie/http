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

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test case for {@link Header}.
 *
 * @since 0.1
 */
final class HeaderTest {

    @ParameterizedTest
    @CsvSource({
        "abc:xyz,abc:xyz,true",
        "abc:xyz,ABC:xyz,true",
        "ABC:xyz,abc:xyz,true",
        "abc:xyz,abc: xyz,true",
        "abc:xyz,foo:bar,false",
        "abc:xyz,abc:bar,false",
        "abc:xyz,abc:XYZ,false",
        "abc:xyz,foo:xyz,false",
        "abc:xyz,abc:xyz ,true"
    })
    void shouldBeEqual(final String one, final String another, final boolean equal) {
        MatcherAssert.assertThat(
            fromString(one).equals(fromString(another)),
            new IsEqual<>(equal)
        );
        MatcherAssert.assertThat(
            fromString(one).hashCode() == fromString(another).hashCode(),
            new IsEqual<>(equal)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "abc,abc",
        " abc,abc",
        "\tabc,abc",
        "abc ,abc "
    })
    void shouldTrimValueLeadingWhitespaces(final String original, final String expected) {
        MatcherAssert.assertThat(
            new Header("whatever", original).getValue(),
            new IsEqual<>(expected)
        );
    }

    @Test
    void toStringHeader() throws Exception {
        MatcherAssert.assertThat(
            new Header("name", "value").toString(),
            new IsEqual<>("name: value")
        );
    }

    private static Header fromString(final String raw) {
        final String[] split = raw.split(":");
        return new Header(split[0], split[1]);
    }

}
