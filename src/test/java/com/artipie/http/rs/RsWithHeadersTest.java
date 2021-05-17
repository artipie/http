/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rs;

import com.artipie.http.headers.Header;
import com.artipie.http.hm.RsHasHeaders;
import org.cactoos.map.MapEntry;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link RsWithHeaders}.
 * @since 0.9
 */
public class RsWithHeadersTest {

    @Test
    void testRsWithHeadersMapEntry() {
        final String name = "Content-Type";
        final String value = "text/plain; charset=us-ascii";
        MatcherAssert.assertThat(
            new RsWithHeaders(new RsWithStatus(RsStatus.OK), new MapEntry<>(name, value)),
            new RsHasHeaders(new Header(name, value))
        );
    }
}
