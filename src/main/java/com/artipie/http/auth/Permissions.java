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

/**
 * Authorization mechanism allows to verify that user
 * can perform some action.
 * @since 0.8
 */
public interface Permissions {

    /**
     * Allow to perform all actions by all users.
     */
    Permissions FREE = (name, action) -> true;

    /**
     * Check if user allowed to perform action.
     * @param name User name
     * @param action Action to perform
     * @return True if allowed
     */
    boolean allowed(String name, String action);

    /**
     * Abstract decorator for Permissions.
     *
     * @since 0.15
     */
    abstract class Wrap implements Permissions {

        /**
         * Origin permissions.
         */
        private final Permissions origin;

        /**
         * Ctor.
         *
         * @param origin Origin permissions.
         */
        protected Wrap(final Permissions origin) {
            this.origin = origin;
        }

        @Override
        public final boolean allowed(final String name, final String action) {
            return this.origin.allowed(name, action);
        }
    }

    /**
     * Permissions implementation allowing single action for single user.
     *
     * @since 0.15
     */
    final class Single implements Permissions {

        /**
         * User name.
         */
        private final String username;

        /**
         * Allowed action.
         */
        private final String action;

        /**
         * Ctor.
         *
         * @param username User name.
         * @param allowed Allowed action.
         */
        public Single(final String username, final String allowed) {
            this.username = username;
            this.action = allowed;
        }

        @Override
        public boolean allowed(final String name, final String act) {
            return this.username.equals(name) && this.action.equals(act);
        }
    }
}
