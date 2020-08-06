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
package com.artipie.http.hm;

import com.artipie.asto.Content;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLine;
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
                slice -> new StatefulResponse(
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
