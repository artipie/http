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

import java.util.Optional;

/**
 * Authentication mechanism to verify user.
 * @since 0.8
 */
public interface Authentication {

    /**
     * Find user by credentials.
     * @param username Username
     * @param password Password
     * @return User login if found
     */
    Optional<String> user(String username, String password);

    /**
     * Abstract decorator for Authentication.
     *
     * @since 0.15
     */
    abstract class Wrap implements Authentication {

        /**
         * Origin authentication.
         */
        private final Authentication auth;

        /**
         * Ctor.
         *
         * @param auth Origin authentication.
         */
        protected Wrap(final Authentication auth) {
            this.auth = auth;
        }

        @Override
        public final Optional<String> user(final String username, final String password) {
            return this.auth.user(username, password);
        }
    }

    /**
     * Authentication implementation aware of single user with specified password.
     *
     * @since 0.15
     */
    final class Single implements Authentication {

        /**
         * Username.
         */
        private final String username;

        /**
         * Password.
         */
        private final String password;

        /**
         * Ctor.
         *
         * @param username Username.
         * @param password Password.
         */
        public Single(final String username, final String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public Optional<String> user(final String user, final String pass) {
            return Optional.of(user)
                .filter(this.username::equals)
                .filter(ignored -> this.password.equals(pass));
        }
    }
}
