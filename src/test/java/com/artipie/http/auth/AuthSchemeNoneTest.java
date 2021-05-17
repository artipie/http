/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import com.artipie.http.Headers;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AuthScheme#NONE}.
 *
 * @since 0.18
 */
final class AuthSchemeNoneTest {

    @Test
    void shouldAuthEmptyHeadersAsAnonymous() {
        MatcherAssert.assertThat(
            AuthScheme.NONE.authenticate(Headers.EMPTY)
                .toCompletableFuture().join()
                .user()
                .map(Authentication.User::name),
            new IsEqual<>(Optional.of("anonymous"))
        );
    }
}
