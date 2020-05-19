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
package com.artipie.http.auth;

import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.slice.SliceSimple;
import io.reactivex.Flowable;
import java.util.Collections;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SliceAuth}.
 *
 * @since 0.8
 */
public final class SliceAuthTest {

    @Test
    void proxyToOriginSliceIfAllowed() {
        MatcherAssert.assertThat(
            new SliceAuth(
                new SliceSimple(new RsWithStatus(RsStatus.OK)),
                new Permission.ByName("any", Permissions.FREE),
                Identities.ANONYMOUS
            ).response(
                new RequestLine("GET", "/foo", "HTTP/1.1").toString(),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new RsHasStatus(RsStatus.OK)
        );
    }

    @Test
    void returnsUnauthorizedErrorIfUnableToAuthenticate() {
        MatcherAssert.assertThat(
            new SliceAuth(
                new SliceSimple(new RsWithStatus(RsStatus.OK)),
                new Permission.ByName("none", (name, action) -> false),
                (line, headers) -> Optional.empty()
            ).response(
                new RequestLine("POST", "/bar", "HTTP/1.2").toString(),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new RsHasStatus(RsStatus.UNAUTHORIZED)
        );
    }

    @Test
    void returnsForbiddenIfNotAllowed() {
        MatcherAssert.assertThat(
            new SliceAuth(
                new SliceSimple(new RsWithStatus(RsStatus.OK)),
                new Permission.ByName("action", (name, action) -> false),
                Identities.ANONYMOUS
            ).response(
                new RequestLine("DELETE", "/baz", "HTTP/1.3").toString(),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new RsHasStatus(RsStatus.FORBIDDEN)
        );
    }
}
