/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rq;

import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link RqParams}.
 *
 * @since 0.18
 */
public final class RqParamsTest {

    @ParameterizedTest
    @CsvSource({
        ",",
        "'',",
        "some=param,",
        "foo=bar,bar",
        "foo=,''",
        "some=param&foo=123,123",
        "foo=bar&foo=baz,bar"
    })
    void findsParamValue(final String query, final String expected) {
        MatcherAssert.assertThat(
            new RqParams(query).value("foo"),
            new IsEqual<>(Optional.ofNullable(expected))
        );
    }
}
