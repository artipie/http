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
package com.artipie.http.hm;

import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.cactoos.map.MapEntry;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RsHasHeaders}.
 *
 * @since 0.8
 */
class RsHasHeadersTest {

    @Test
    void shouldMatchHeaders() {
        final MapEntry<String, String> type = new MapEntry<>(
            "Content-Type", "application/json"
        );
        final MapEntry<String, String> length = new MapEntry<>(
            "Content-Length", "123"
        );
        final Response response = new RsWithHeaders(
            new RsWithStatus(RsStatus.OK),
            Arrays.asList(type, length)
        );
        final RsHasHeaders matcher = new RsHasHeaders(new Headers.From(length, type));
        MatcherAssert.assertThat(
            matcher.matches(response),
            new IsEqual<>(true)
        );
    }

    @Test
    void shouldNotMatchNotMatchingHeaders() {
        final Response response = new RsWithStatus(RsStatus.OK);
        final RsHasHeaders matcher = new RsHasHeaders(
            Matchers.containsInAnyOrder(new MapEntry<>("X-My-Header", "value"))
        );
        MatcherAssert.assertThat(
            matcher.matches(response),
            new IsEqual<>(false)
        );
    }

    @Test
    void shouldMatchHeadersByValue() {
        final String key = "k";
        final String value = "v";
        final Response response = new RsWithHeaders(
            new RsWithStatus(RsStatus.OK),
            Collections.singleton(new EntryWithoutEquals(key, value))
        );
        final RsHasHeaders matcher = new RsHasHeaders(new EntryWithoutEquals(key, value));
        MatcherAssert.assertThat(
            matcher.matches(response),
            new IsEqual<>(true)
        );
    }

    /**
     * Implementation of {@link Map.Entry} with default equals & hashCode.
     *
     * @since 0.8
     */
    private static class EntryWithoutEquals implements Map.Entry<String, String> {

        /**
         * Key.
         */
        private final String key;

        /**
         * Value.
         */
        private final String value;

        EntryWithoutEquals(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getValue() {
            return this.value;
        }

        @Override
        public String setValue(final String ignored) {
            throw new UnsupportedOperationException();
        }
    }
}
