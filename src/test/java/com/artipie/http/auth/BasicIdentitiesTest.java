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
package com.artipie.http.auth;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.cactoos.iterable.IterableOf;
import org.cactoos.map.MapEntry;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link BasicIdentities}.
 *
 * @since 0.8
 */
public final class BasicIdentitiesTest {

    /**
     * Stub for interface.
     */
    private final Authentication auth = (username, password) -> Optional.of(username);

    @Test
    void userWithEmptyHeaders() {
        final Iterable<Map.Entry<String, String>> headers = new IterableOf<Entry<String, String>>(
            new MapEntry<String, String>("x-header", "header")
        );
        MatcherAssert.assertThat(
            new BasicIdentities(this.auth).user("", headers),
            new IsEqual<>(Optional.empty())
        );
    }

    /**
     * Implementation to find and parse header.
     * Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
     * Here the user agent uses 'Aladdin' as the username and 'open sesame' as the password.
     */
    @Test
    void userWithBasicHeaders() {
        final Iterable<Map.Entry<String, String>> headers = new IterableOf<Entry<String, String>>(
            new MapEntry<String, String>("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==")
        );
        MatcherAssert.assertThat(
            new BasicIdentities(this.auth).user("", headers),
            new IsEqual<>(Optional.of("Aladdin"))
        );
    }
}
