/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.http.hm;

import com.artipie.http.Headers;
import com.artipie.http.headers.Header;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.rs.StandardRs;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ResponseMatcher}.
 * @since 0.10
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
class ResponseMatcherTest {

    @Test
    void matchesStatusAndHeaders() {
        final Header header = new Header("Mood", "sunny");
        final RsStatus status = RsStatus.CREATED;
        MatcherAssert.assertThat(
            new ResponseMatcher(RsStatus.CREATED, header)
                .matches(
                    new RsWithHeaders(new RsWithStatus(status), header)
                ),
            new IsEqual<>(true)
        );
    }

    @Test
    void matchesHeaders() {
        final Header header = new Header("Type", "string");
        MatcherAssert.assertThat(
            new ResponseMatcher(header)
                .matches(
                    new RsWithHeaders(StandardRs.EMPTY, header)
                ),
            new IsEqual<>(true)
        );
    }

    @Test
    void matchesByteBody() {
        final String body = "111";
        MatcherAssert.assertThat(
            new ResponseMatcher(body.getBytes())
                .matches(
                    new RsWithBody(
                        StandardRs.EMPTY, body, StandardCharsets.UTF_8
                    )
                ),
            new IsEqual<>(true)
        );
    }

    @Test
    void matchesStringBody() {
        final String body = "000";
        MatcherAssert.assertThat(
            new ResponseMatcher(body, StandardCharsets.UTF_8)
                .matches(
                    new RsWithBody(
                        StandardRs.EMPTY, body, StandardCharsets.UTF_8
                    )
                ),
            new IsEqual<>(true)
        );
    }

    @Test
    void matchesStatusAndStringBody() {
        final String body = "def";
        MatcherAssert.assertThat(
            new ResponseMatcher(RsStatus.NOT_FOUND, body, StandardCharsets.UTF_8)
                .matches(
                    new RsWithBody(
                        StandardRs.NOT_FOUND, body, StandardCharsets.UTF_8
                    )
                ),
            new IsEqual<>(true)
        );
    }

    @Test
    void matchesStatusAndByteBody() {
        final String body = "abc";
        MatcherAssert.assertThat(
            new ResponseMatcher(RsStatus.OK, body.getBytes())
                .matches(
                    new RsWithBody(
                        StandardRs.EMPTY, body, StandardCharsets.UTF_8
                    )
                ),
            new IsEqual<>(true)
        );
    }

    @Test
    void matchesStatusBodyAndHeaders() {
        final String body = "123";
        MatcherAssert.assertThat(
            new ResponseMatcher(RsStatus.OK, body.getBytes())
                .matches(
                    new RsWithBody(
                        new RsWithHeaders(
                            StandardRs.EMPTY,
                            new Header("Content-Length", "3")
                        ),
                        body, StandardCharsets.UTF_8
                    )
                ),
            new IsEqual<>(true)
        );
    }

    @Test
    void matchesStatusAndHeaderMatcher() {
        final RsStatus status = RsStatus.ACCEPTED;
        final String header = "Some-header";
        final String value = "Some value";
        MatcherAssert.assertThat(
            new ResponseMatcher(status, new IsHeader(header, value))
                .matches(
                    new RsWithHeaders(
                        new RsWithStatus(status),
                        new Headers.From(header, value)
                    )
                ),
            new IsEqual<>(true)
        );
    }
}
