/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Authentication mechanism to verify user.
 * @since 0.8
 */
public interface Authentication {

    /**
     * Resolve anyone as an anonymous user.
     */
    Authentication ANONYMOUS = (name, pswd) -> Optional.of(new Authentication.User("anonymous"));

    /**
     * Find user by credentials.
     * @param username Username
     * @param password Password
     * @return User login if found
     */
    Optional<User> user(String username, String password);

    /**
     * User.
     * @since 0.16
     */
    final class User {

        /**
         * User name.
         */
        private final String uname;

        /**
         * User groups.
         */
        private final Collection<String> ugroups;

        /**
         * Ctro.
         * @param name Name of the user
         * @param groups User groups
         */
        public User(final String name, final Collection<String> groups) {
            this.uname = name;
            this.ugroups = groups;
        }

        /**
         * Ctor.
         * @param name User anme
         */
        public User(final String name) {
            this(name, Collections.emptyList());
        }

        /**
         * Ger user name.
         * @return Name
         */
        public String name() {
            return this.uname;
        }

        /**
         * Get user groups.
         * @return User groups
         */
        public Collection<String> groups() {
            return this.ugroups;
        }

        @Override
        public boolean equals(final Object other) {
            final boolean res;
            if (this == other) {
                res = true;
            } else if (other == null || this.getClass() != other.getClass()) {
                res = false;
            } else {
                final User user = (User) other;
                res = Objects.equals(this.uname, user.uname)
                    && Objects.equals(this.ugroups, user.ugroups);
            }
            return res;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.uname, this.ugroups);
        }
    }

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
        public final Optional<User> user(final String username, final String password) {
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
         * User.
         */
        private final User user;

        /**
         * Password.
         */
        private final String password;

        /**
         * Ctor.
         *
         * @param user Username.
         * @param password Password.
         */
        public Single(final String user, final String password) {
            this(new User(user), password);
        }

        /**
         * Ctor.
         *
         * @param user User
         * @param password Password
         */
        public Single(final User user, final String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public Optional<User> user(final String name, final String pass) {
            return Optional.of(name)
                .filter(item -> item.equals(this.user.uname))
                .filter(ignored -> this.password.equals(pass))
                .map(ignored -> this.user);
        }
    }

    /**
     * Joined authentication composes multiple authentication instances into single one.
     * User authenticated if any of authentication instances authenticates the user.
     *
     * @since 0.16
     */
    final class Joined implements Authentication {

        /**
         * Origin authentications.
         */
        private final List<Authentication> origins;

        /**
         * Ctor.
         *
         * @param origins Origin authentications.
         */
        public Joined(final Authentication... origins) {
            this(Arrays.asList(origins));
        }

        /**
         * Ctor.
         *
         * @param origins Origin authentications.
         */
        public Joined(final List<Authentication> origins) {
            this.origins = origins;
        }

        @Override
        public Optional<User> user(final String user, final String pass) {
            return this.origins.stream()
                .map(auth -> auth.user(user, pass))
                .flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty))
                .findFirst();
        }

        @Override
        public String toString() {
            return String.format(
                "%s([%s])",
                this.getClass().getSimpleName(),
                this.origins.stream().map(Object::toString).collect(Collectors.joining(","))
            );
        }
    }
}
