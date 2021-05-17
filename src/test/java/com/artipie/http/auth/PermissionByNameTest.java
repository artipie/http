/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test for {@link Permission.ByName}.
 * @since 0.16
 */
class PermissionByNameTest {

    @ParameterizedTest
    @CsvSource({
        "READ,download,john",
        "READ,r,john",
        "READ,install,john",
        "WRITE,w,alice",
        "WRITE,write,alice",
        "WRITE,upload,alice",
        "WRITE,publish,alice",
        "WRITE,push,alice",
        "WRITE,deploy,mark",
        "DELETE,delete,mark",
        "DELETE,d,mark"
    })
    void checksAllActions(final Action.Standard action, final String perm, final String name) {
        MatcherAssert.assertThat(
            new Permission.ByName(
                (identity, act) -> name.equals(identity.name()) && act.equals(perm),
                action
            ).allowed(new Authentication.User(name)),
            new IsEqual<>(true)
        );
    }

    @Test
    void doesNotAllowWhenActionIsNotPresent() {
        final String user = "jane";
        MatcherAssert.assertThat(
            new Permission.ByName(
                (identity, act) -> user.equals(identity.name()) && act.equals("a"),
                () -> Arrays.asList("b", "c")
            ).allowed(new Authentication.User(user)),
            new IsEqual<>(false)
        );
    }

}
