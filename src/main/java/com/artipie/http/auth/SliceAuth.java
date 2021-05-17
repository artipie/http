/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.headers.WwwAuthenticate;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Map;
import org.reactivestreams.Publisher;

/**
 * Slice wrapper with authorization and authentication.
 * <p>
 * Example: the class which allows upload only for users with 'upload' permissions
 * and resolves user identity by "http-basic" mechanism:
 * </p>
 * <pre><code>
 * new SliceAuth(
 *   new SliceUpload(storage),
 *   new Permission.ByName("upload", permissions),
 *   new AuthBasic(passwords)
 * );
 * </code></pre>
 * @since 0.8
 * @deprecated Use {@link BasicAuthSlice} instead
 */
@Deprecated
public final class SliceAuth implements Slice {

    /**
     * Origin slice.
     */
    private final Slice origin;

    /**
     * Authorization.
     */
    private final Permission perm;

    /**
     * Authentication.
     */
    private final Identities ids;

    /**
     * Ctor.
     * @param origin Origin slice
     * @param perm Authorization mechanism
     * @param ids Authentication mechanism
     */
    public SliceAuth(final Slice origin, final Permission perm, final Identities ids) {
        this.origin = origin;
        this.ids = ids;
        this.perm = perm;
    }

    @Override
    public Response response(final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        return this.ids.user(line, headers)
            .map(this.perm::allowed).map(
                allowed -> {
                    final Response rsp;
                    if (allowed) {
                        rsp = this.origin.response(line, headers, body);
                    } else {
                        rsp = new RsWithStatus(RsStatus.FORBIDDEN);
                    }
                    return rsp;
                }
            ).orElseGet(
                () -> {
                    final Response rsp;
                    if (this.perm.allowed(Permissions.ANY_USER)) {
                        rsp = this.origin.response(line, headers, body);
                    } else {
                        rsp = new RsWithHeaders(
                            new RsWithStatus(RsStatus.UNAUTHORIZED),
                            new Headers.From(new WwwAuthenticate("Basic"))
                        );
                    }
                    return rsp;
                }
            );
    }
}
