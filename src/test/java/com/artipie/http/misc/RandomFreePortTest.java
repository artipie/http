/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.misc;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link RandomFreePort}.
 * @since 0.18
 */
final class RandomFreePortTest {
    @Test
    void returnsFreePort() {
        MatcherAssert.assertThat(
            new RandomFreePort().get(),
            new IsInstanceOf(Integer.class)
        );
    }
}
