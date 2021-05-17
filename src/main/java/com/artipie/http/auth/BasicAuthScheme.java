/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import com.artipie.http.headers.Authorization;
import com.artipie.http.rq.RqHeaders;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Basic authentication method.
 *
 * @since 0.17
 */
public final class BasicAuthScheme implements AuthScheme {

    /**
     * Basic authentication prefix.
     */
    public static final String NAME = "Basic";

    /**
     * Authentication.
     */
    private final Authentication auth;

    /**
     * Ctor.
     * @param auth Authentication.
     */
    public BasicAuthScheme(final Authentication auth) {
        this.auth = auth;
    }

    @Override
    public CompletionStage<Result> authenticate(final Iterable<Map.Entry<String, String>> headers) {
        return CompletableFuture.completedFuture(
            this.user(headers).<Result>map(Success::new).orElseGet(Failure::new)
        );
    }

    /**
     * Obtains user from authentication header.
     * @param headers Headers
     * @return User if authorised
     */
    private Optional<Authentication.User> user(final Iterable<Map.Entry<String, String>> headers) {
        return new RqHeaders(headers, Authorization.NAME).stream()
            .findFirst()
            .map(Authorization::new)
            .filter(hdr -> hdr.scheme().equals(BasicAuthScheme.NAME))
            .map(hdr -> new Authorization.Basic(hdr.credentials()))
            .flatMap(hdr -> this.auth.user(hdr.username(), hdr.password()));
    }

    /**
     * Successful result with authenticated user.
     *
     * @since 0.17
     */
    private static class Success implements Result {

        /**
         * Authenticated user.
         */
        private final Authentication.User usr;

        /**
         * Ctor.
         *
         * @param user Authenticated user.
         */
        Success(final Authentication.User user) {
            this.usr = user;
        }

        @Override
        public Optional<Authentication.User> user() {
            return Optional.of(this.usr);
        }

        @Override
        public String challenge() {
            return BasicAuthScheme.NAME;
        }
    }

    /**
     * Failed result without authenticated user.
     *
     * @since 0.17
     */
    private static class Failure implements Result {

        @Override
        public Optional<Authentication.User> user() {
            return Optional.empty();
        }

        @Override
        public String challenge() {
            return BasicAuthScheme.NAME;
        }
    }
}
