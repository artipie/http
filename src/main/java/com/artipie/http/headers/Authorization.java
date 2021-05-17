/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.headers;

import com.artipie.http.Headers;
import com.artipie.http.auth.BasicAuthScheme;
import com.artipie.http.auth.BearerAuthScheme;
import com.artipie.http.rq.RqHeaders;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cactoos.text.Base64Decoded;
import org.cactoos.text.Base64Encoded;

/**
 * Authorization header.
 *
 * @since 0.12
 */
public final class Authorization extends Header.Wrap {

    /**
     * Header name.
     */
    public static final String NAME = "Authorization";

    /**
     * Header value RegEx.
     */
    private static final Pattern VALUE = Pattern.compile("(?<scheme>[^ ]+) (?<credentials>.+)");

    /**
     * Ctor.
     *
     * @param scheme Authentication scheme.
     * @param credentials Credentials.
     */
    public Authorization(final String scheme, final String credentials) {
        super(new Header(Authorization.NAME, String.format("%s %s", scheme, credentials)));
    }

    /**
     * Ctor.
     *
     * @param value Header value.
     */
    public Authorization(final String value) {
        super(new Header(Authorization.NAME, value));
    }

    /**
     * Ctor.
     *
     * @param headers Headers to extract header from.
     */
    public Authorization(final Headers headers) {
        this(new RqHeaders.Single(headers, Authorization.NAME).asString());
    }

    /**
     * Read scheme from header value.
     *
     * @return Scheme string.
     */
    public String scheme() {
        return this.matcher().group("scheme");
    }

    /**
     * Read credentials from header value.
     *
     * @return Credentials string.
     */
    public String credentials() {
        return this.matcher().group("credentials");
    }

    /**
     * Creates matcher for header value.
     *
     * @return Matcher for header value.
     */
    private Matcher matcher() {
        final String value = this.getValue();
        final Matcher matcher = VALUE.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalStateException(
                String.format("Failed to parse header value: %s", value)
            );
        }
        return matcher;
    }

    /**
     * Basic authentication `Authorization` header.
     *
     * @since 0.12
     */
    public static final class Basic extends Header.Wrap {

        /**
         * Ctor.
         *
         * @param username User name.
         * @param password Password.
         */
        public Basic(final String username, final String password) {
            this(new Base64Encoded(String.format("%s:%s", username, password)).toString());
        }

        /**
         * Ctor.
         *
         * @param credentials Credentials.
         */
        public Basic(final String credentials) {
            super(new Authorization(BasicAuthScheme.NAME, credentials));
        }

        /**
         * Read credentials from header value.
         *
         * @return Credentials string.
         */
        public String credentials() {
            return new Authorization(this.getValue()).credentials();
        }

        /**
         * Read username from header value.
         *
         * @return Username string.
         */
        public String username() {
            return this.tokens()[0];
        }

        /**
         * Read password from header value.
         *
         * @return Password string.
         */
        public String password() {
            return this.tokens()[1];
        }

        /**
         * Read tokens from decoded credentials.
         *
         * @return Tokens array.
         */
        private String[] tokens() {
            return new Base64Decoded(this.credentials()).toString().split(":");
        }
    }

    /**
     * Bearer authentication `Authorization` header.
     *
     * @since 0.12
     */
    public static final class Bearer extends Header.Wrap {

        /**
         * Ctor.
         *
         * @param token Token.
         */
        public Bearer(final String token) {
            super(new Authorization(BearerAuthScheme.NAME, token));
        }

        /**
         * Read token from header value.
         *
         * @return Token string.
         */
        public String token() {
            return new Authorization(this.getValue()).credentials();
        }
    }
}
