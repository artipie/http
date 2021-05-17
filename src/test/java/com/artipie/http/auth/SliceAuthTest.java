/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import com.artipie.http.headers.Header;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.slice.SliceSimple;
import io.reactivex.Flowable;
import java.util.Collections;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SliceAuth}.
 *
 * @since 0.8
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("deprecation")
public final class SliceAuthTest {

    @Test
    void proxyToOriginSliceIfAllowed() {
        MatcherAssert.assertThat(
            new SliceAuth(
                new SliceSimple(new RsWithStatus(RsStatus.OK)),
                user -> true,
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
                user -> false,
                (line, headers) -> Optional.empty()
            ).response(
                new RequestLine("POST", "/bar", "HTTP/1.2").toString(),
                Collections.emptyList(),
                Flowable.empty()
            ),
            Matchers.allOf(
                new RsHasStatus(RsStatus.UNAUTHORIZED),
                new RsHasHeaders(new Header("WWW-Authenticate", "Basic"))
            )
        );
    }

    @Test
    void returnsForbiddenIfNotAllowed() {
        MatcherAssert.assertThat(
            new SliceAuth(
                new SliceSimple(new RsWithStatus(RsStatus.OK)),
                new Permission.ByName("read", (name, action) -> false),
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
