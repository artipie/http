/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Authentication scheme such as Basic, Bearer etc.
 *
 * @since 0.17
 */
public interface AuthScheme {

    /**
     * Absent auth scheme that authenticates any request as "anonymous" user.
     */
    AuthScheme NONE = ignored -> CompletableFuture.completedFuture(
        new AuthScheme.Result() {
            @Override
            public Optional<Authentication.User> user() {
                return Optional.of(new Authentication.User("anonymous"));
            }

            @Override
            public String challenge() {
                throw new UnsupportedOperationException();
            }
        }
    );

    /**
     * Authenticate HTTP request by it's headers.
     *
     * @param headers Request headers.
     * @return Authentication result.
     */
    CompletionStage<Result> authenticate(Iterable<Map.Entry<String, String>> headers);

    /**
     * HTTP request authentication result.
     *
     * @since 0.17
     */
    interface Result {

        /**
         * Authenticated user.
         *
         * @return Authenticated user, empty if not authenticated.
         */
        Optional<Authentication.User> user();

        /**
         * Get authentication challenge that is provided in response WWW-Authenticate header value.
         *
         * @return Authentication challenge for client.
         */
        String challenge();
    }

    /**
     * Fake implementation of {@link AuthScheme}.
     * @since 0.17.5
     */
    final class Fake implements AuthScheme {

        /**
         * Fake challange constant.
         */
        public static final String FAKE_CHLLNG = "fake";

        /**
         * Optional of User.
         */
        private final Optional<Authentication.User> usr;

        /**
         * Challenge.
         */
        private final String chllng;

        /**
         * Ctor.
         * @param usr User
         * @param chllng Challenge
         */
        public Fake(final Optional<Authentication.User> usr, final String chllng) {
            this.usr = usr;
            this.chllng = chllng;
        }

        /**
         * Ctor.
         * @param name User name
         */
        public Fake(final String name) {
            this(Optional.of(new Authentication.User(name)), Fake.FAKE_CHLLNG);
        }

        /**
         * Ctor.
         */
        public Fake() {
            this(Optional.empty(), Fake.FAKE_CHLLNG);
        }

        @Override
        public CompletionStage<Result> authenticate(
            final Iterable<Map.Entry<String, String>> headers
        ) {
            return CompletableFuture.completedFuture(
                new AuthScheme.Result() {
                    @Override
                    public Optional<Authentication.User> user() {
                        return Fake.this.usr;
                    }

                    @Override
                    public String challenge() {
                        return Fake.this.chllng;
                    }
                }
            );
        }
    }
}
