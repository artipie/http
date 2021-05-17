/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rs;

import com.artipie.asto.Content;
import com.artipie.http.Headers;
import com.artipie.http.headers.ContentLength;
import com.artipie.http.hm.RsHasHeaders;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RsFull}.
 *
 * @since 0.18
 */
final class RsFullTest {

    @Test
    void appendsContentSizeHeaderForContentBody() {
        final int size = 100;
        MatcherAssert.assertThat(
            new RsFull(
                RsStatus.OK,
                Headers.EMPTY,
                new Content.From(new byte[size])
            ),
            new RsHasHeaders(new ContentLength(size))
        );
    }
}
