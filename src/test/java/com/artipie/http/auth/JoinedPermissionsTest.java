/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import com.google.common.collect.Streams;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test case for {@link JoinedPermissions}.
 *
 * @since 0.18
 */
final class JoinedPermissionsTest {

    @ParameterizedTest
    @CsvSource({
        "allow,absent,true",
        "allow,allow,true",
        "allow,deny,true",
        "deny,absent,false",
        "deny,deny,false",
        "absent,absent,true"
    })
    void shouldAllowWhenExpected(final String one, final String two, final boolean allow) {
        MatcherAssert.assertThat(
            new JoinedPermissions(
                Streams.concat(fake(one), fake(two)).collect(Collectors.toList())
            ).allowed(new Authentication.User("some name"), "some action"),
            new IsEqual<>(allow)
        );
    }

    // @checkstyle ReturnCountCheck (2 lines)
    @SuppressWarnings("PMD.OnlyOneReturn")
    private static Stream<Permissions> fake(final String code) {
        switch (code) {
            case "absent":
                return Stream.empty();
            case "allow":
                return Stream.of((name, action) -> true);
            case "deny":
                return Stream.of((name, action) -> false);
            default:
                throw new IllegalArgumentException(String.format("Unsupported code: %s", code));
        }
    }
}
