/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import com.artipie.asto.Content;
import com.artipie.http.Headers;
import com.artipie.http.headers.Authorization;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.hm.SliceHasResponse;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.rs.StandardRs;
import com.artipie.http.slice.SliceSimple;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link BasicAuthSlice}.
 * @since 0.17
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class BasicAuthSliceTest {

    @Test
    void proxyToOriginSliceIfAllowed() {
        MatcherAssert.assertThat(
            new BasicAuthSlice(
                new SliceSimple(StandardRs.OK),
                (user, pswd) -> Optional.empty(),
                user -> true
            ),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.OK),
                new RequestLine("GET", "/foo")
            )
        );
    }

    @Test
    void returnsUnauthorizedErrorIfUnableToAuthenticate() {
        MatcherAssert.assertThat(
            new BasicAuthSlice(
                new SliceSimple(StandardRs.OK),
                (user, pswd) -> Optional.empty(),
                user -> false
            ),
            new SliceHasResponse(
                Matchers.allOf(
                    new RsHasStatus(RsStatus.UNAUTHORIZED),
                    new RsHasHeaders(new Header("WWW-Authenticate", "Basic"))
                ),
                new RequestLine("POST", "/bar", "HTTP/1.2")
            )
        );
    }

    @Test
    void returnsForbiddenIfNotAllowed() {
        final String name = "john";
        MatcherAssert.assertThat(
            new BasicAuthSlice(
                new SliceSimple(new RsWithStatus(RsStatus.OK)),
                (user, pswd) -> Optional.of(new Authentication.User(name)),
                user -> false
            ),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.FORBIDDEN),
                new RequestLine("DELETE", "/baz", "HTTP/1.3"),
                new Headers.From(new Authorization.Basic(name, "123")),
                Content.EMPTY
            )
        );
    }

    @Test
    void parsesHeaders() {
        final String aladdin = "Aladdin";
        final String pswd = "open sesame";
        MatcherAssert.assertThat(
            new BasicAuthSlice(
                new SliceSimple(StandardRs.OK),
                new Authentication.Single(aladdin, pswd),
                user -> true
            ),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.OK),
                new RequestLine("PUT", "/my-endpoint"),
                new Headers.From(new Authorization.Basic(aladdin, pswd)),
                Content.EMPTY
            )
        );
    }

    @Test
    void doesNotAuthenticateIfNotNeeded() {
        MatcherAssert.assertThat(
            new BasicAuthSlice(
                new SliceSimple(StandardRs.OK),
                (user, pswd) -> {
                    throw new IllegalStateException("Should not be invoked");
                },
                user -> user.equals(Permissions.ANY_USER)
            ),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.OK),
                new RequestLine(RqMethod.GET, "/resource"),
                new Headers.From(new Authorization.Basic("alice", "12345")),
                Content.EMPTY
            )
        );
    }
}
