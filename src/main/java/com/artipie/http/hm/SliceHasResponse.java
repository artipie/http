/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.hm;

import com.artipie.asto.Content;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.CachedResponse;
import io.reactivex.Flowable;
import org.cactoos.func.StickyFunc;
import org.cactoos.func.UncheckedFunc;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for {@link Slice} response.
 * @since 0.16
 */
public final class SliceHasResponse extends TypeSafeMatcher<Slice> {

    /**
     * Response matcher.
     */
    private final Matcher<? extends Response> rsp;

    /**
     * Response function from slice.
     */
    private final UncheckedFunc<Slice, Response> target;

    /**
     * New response matcher for slice with request line.
     * @param rsp Response matcher
     * @param line Request line
     */
    public SliceHasResponse(final Matcher<? extends Response> rsp, final RequestLine line) {
        this(rsp, line, Headers.EMPTY, new Content.From(Flowable.empty()));
    }

    /**
     * New response matcher for slice with request line, headers and body.
     * @param rsp Response matcher
     * @param line Request line
     * @param headers Headers
     * @param body Body
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public SliceHasResponse(final Matcher<? extends Response> rsp, final RequestLine line,
        final Headers headers, final Content body) {
        this.rsp = rsp;
        this.target = new UncheckedFunc<>(
            new StickyFunc<>(
                slice -> new CachedResponse(
                    slice.response(line.toString(), headers, body)
                )
            )
        );
    }

    @Override
    public boolean matchesSafely(final Slice item) {
        return this.rsp.matches(this.target.apply(item));
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("response: ").appendDescriptionOf(this.rsp);
    }

    @Override
    public void describeMismatchSafely(final Slice item, final Description description) {
        description.appendText("response was: ").appendValue(this.target.apply(item));
    }
}
