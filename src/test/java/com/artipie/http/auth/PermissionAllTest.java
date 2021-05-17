/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import com.google.common.base.Splitter;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test for {@link Permission.All}.
 *
 * @since 0.17
 */
class PermissionAllTest {

    @ParameterizedTest
    @CsvSource({
        "'',true",
        "false,false",
        "true,true",
        "false;false,false",
        "false;true,false",
        "true;false,false",
        "true;true,true"
    })
    void shouldReturnExpectedResult(final String values, final boolean expected) {
        MatcherAssert.assertThat(
            new Permission.All(
                StreamSupport.stream(
                    Splitter.on(";").omitEmptyStrings().split(values).spliterator(),
                    false
                ).map(
                    str -> (Permission) user -> Boolean.parseBoolean(str)
                ).collect(Collectors.toList())
            ).allowed(new Authentication.User("alice")),
            new IsEqual<>(expected)
        );
    }
}
