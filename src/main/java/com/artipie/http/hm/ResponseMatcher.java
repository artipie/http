/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.hm;

import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import java.nio.charset.Charset;
import java.util.Map;
import org.cactoos.list.ListOf;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;

/**
 * Response matcher.
 * @since 0.10
 */
public final class ResponseMatcher extends AllOf<Response> {

    /**
     * Ctor.
     *
     * @param status Expected status
     * @param headers Expected headers
     * @param body Expected body
     */
    public ResponseMatcher(
        final RsStatus status,
        final Iterable<? extends Map.Entry<String, String>> headers,
        final byte[] body
    ) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(status),
                new RsHasHeaders(headers),
                new RsHasBody(body)
            )
        );
    }

    /**
     * Ctor.
     * @param status Expected status
     * @param body Expected body
     * @param headers Expected headers
     */
    @SafeVarargs
    public ResponseMatcher(
        final RsStatus status,
        final byte[] body,
        final Map.Entry<String, String>... headers
    ) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(status),
                new RsHasHeaders(headers),
                new RsHasBody(body)
            )
        );
    }

    /**
     * Ctor.
     * @param status Expected status
     * @param body Expected body
     */
    public ResponseMatcher(final RsStatus status, final byte[] body) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(status),
                new RsHasBody(body)
            )
        );
    }

    /**
     * Ctor.
     * @param status Expected status
     * @param body Expected body
     * @param charset Character set
     */
    public ResponseMatcher(final RsStatus status, final String body, final Charset charset) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(status),
                new RsHasBody(body.getBytes(charset))
            )
        );
    }

    /**
     * Ctor.
     * @param body Expected body
     * @param charset Character set
     */
    public ResponseMatcher(final String body, final Charset charset) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(RsStatus.OK),
                new RsHasBody(body.getBytes(charset))
            )
        );
    }

    /**
     * Ctor.
     * @param body Expected body
     */
    public ResponseMatcher(final byte[] body) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(RsStatus.OK),
                new RsHasBody(body)
            )
        );
    }

    /**
     * Ctor.
     *
     * @param headers Expected headers
     */
    public ResponseMatcher(final Iterable<? extends Map.Entry<String, String>> headers) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(RsStatus.OK),
                new RsHasHeaders(headers)
            )
        );
    }

    /**
     * Ctor.
     * @param headers Expected headers
     */
    @SafeVarargs
    public ResponseMatcher(final Map.Entry<String, String>... headers) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(RsStatus.OK),
                new RsHasHeaders(headers)
            )
        );
    }

    /**
     * Ctor.
     *
     * @param status Expected status
     * @param headers Expected headers
     */
    public ResponseMatcher(
        final RsStatus status,
        final Iterable<? extends Map.Entry<String, String>> headers
    ) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(status),
                new RsHasHeaders(headers)
            )
        );
    }

    /**
     * Ctor.
     * @param status Expected status
     * @param headers Expected headers
     */
    @SafeVarargs
    public ResponseMatcher(final RsStatus status, final Map.Entry<String, String>... headers) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(status),
                new RsHasHeaders(headers)
            )
        );
    }

    /**
     * Ctor.
     * @param status Expected status
     * @param headers Matchers for expected headers
     */
    @SafeVarargs
    public ResponseMatcher(
        final RsStatus status,
        final Matcher<? super Map.Entry<String, String>>... headers
    ) {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(status),
                new RsHasHeaders(headers)
            )
        );
    }
}
