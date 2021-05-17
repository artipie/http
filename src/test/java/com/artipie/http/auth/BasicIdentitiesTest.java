/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
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
@SuppressWarnings("deprecation")
public final class BasicIdentitiesTest {

    /**
     * Stub for interface.
     */
    private final Authentication auth =
        (username, password) -> Optional.of(new Authentication.User(username));

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
            new IsEqual<>(Optional.of(new Authentication.User("Aladdin")))
        );
    }
}
