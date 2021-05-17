/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

/**
 * Authorization mechanism allows to verify that user
 * can perform some action.
 * @since 0.8
 */
public interface Permissions {

    /**
     * Any user instance.
     */
    Authentication.User ANY_USER = new Authentication.User("*");

    /**
     * Allow to perform all actions by all users.
     */
    Permissions FREE = (name, action) -> true;

    /**
     * Check if user is allowed to perform an action.
     * @param user User
     * @param action Action to perform
     * @return True if allowed
     */
    boolean allowed(Authentication.User user, String action);

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
        public final boolean allowed(final Authentication.User user, final String action) {
            return this.origin.allowed(user, action);
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
        public boolean allowed(final Authentication.User user, final String act) {
            return this.username.equals(user.name()) && this.action.equals(act);
        }
    }
}
