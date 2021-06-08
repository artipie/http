/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rs;

import com.artipie.asto.Content;
import com.artipie.http.headers.ContentLength;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.hm.RsHasHeaders;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RsWithBody}.
 * @since 0.9
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
final class RsWithBodyTest {

    @Test
    void createsResponseWithStatusOkAndBody() {
        final byte[] body = "abc".getBytes();
        MatcherAssert.assertThat(
            new RsWithBody(ByteBuffer.wrap(body)),
            new ResponseMatcher(body)
        );
    }

    @Test
    void appendsBody() {
        final String body = "def";
        MatcherAssert.assertThat(
            new RsWithBody(new RsWithStatus(RsStatus.CREATED), body, StandardCharsets.UTF_8),
            new ResponseMatcher(RsStatus.CREATED, body, StandardCharsets.UTF_8)
        );
    }

    @Test
    void appendsContentSizeHeader() {
        final int size = 100;
        MatcherAssert.assertThat(
            new RsWithBody(StandardRs.EMPTY, new Content.From(new byte[size])),
            new RsHasHeaders(new Header("Content-Length", String.valueOf(size)))
        );
    }

    @Test
    void appendsContentSizeHeaderForContentBody() {
        final int size = 17;
        MatcherAssert.assertThat(
            new RsWithBody(new Content.From(new byte[size])),
            new RsHasHeaders(new ContentLength(size))
        );
    }

    @Test
    void overridesContentSizeHeader() {
        final int size = 17;
        MatcherAssert.assertThat(
            new RsWithBody(
                new RsWithHeaders(StandardRs.OK, new ContentLength(100)),
                new Content.From(new byte[size])
            ),
            new RsHasHeaders(new ContentLength(size))
        );
    }
}
