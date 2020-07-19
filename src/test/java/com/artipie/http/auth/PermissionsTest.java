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

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link Permissions}.
 *
 * @since 0.15
 */
public final class PermissionsTest {

    @Test
    void wrapDelegatesToOrigin() {
        final String user = "user";
        final String act = "act";
        final boolean result = true;
        MatcherAssert.assertThat(
            "Result is forwarded from delegate without modification",
            new TestPermissions(
                (username, action) -> {
                    MatcherAssert.assertThat(
                        "Username is forwarded to delegate without modification",
                        username,
                        new IsEqual<>(user)
                    );
                    MatcherAssert.assertThat(
                        "Action is forwarded to delegate without modification",
                        action,
                        new IsEqual<>(act)
                    );
                    return result;
                }
            ).allowed(user, act),
            new IsEqual<>(result)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "Aladdin,read,true",
        "Aladdin,write,false",
        "JohnDoe,read,false"
    })
    void singleAllowsAsExpected(
        final String username,
        final String action,
        final boolean allow
    ) {
        MatcherAssert.assertThat(
            new Permissions.Single("Aladdin", "read").allowed(username, action),
            new IsEqual<>(allow)
        );
    }

    /**
     * Permissions for testing Permissions.Wrap.
     *
     * @since 0.15
     */
    private static final class TestPermissions extends Permissions.Wrap {

        /**
         * Ctor.
         *
         * @param origin Origin permissions.
         */
        TestPermissions(final Permissions origin) {
            super(origin);
        }
    }
}
