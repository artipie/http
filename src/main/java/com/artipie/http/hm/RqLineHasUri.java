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

import com.artipie.http.rq.RequestLineFrom;
import java.net.URI;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsEqual;

/**
 * Request line URI matcher.
 * @since 0.10
 */
public final class RqLineHasUri extends TypeSafeMatcher<RequestLineFrom> {

    /**
     * Request line URI matcher.
     */
    private final Matcher<URI> target;

    /**
     * Match request line against URI matcher.
     * @param target URI matcher
     */
    public RqLineHasUri(final Matcher<URI> target) {
        this.target = target;
    }

    @Override
    public boolean matchesSafely(final RequestLineFrom item) {
        return this.target.matches(item.uri());
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("URI ").appendDescriptionOf(this.target);
    }

    @Override
    public void describeMismatchSafely(final RequestLineFrom item, final Description description) {
        this.target.describeMismatch(item.uri(), description.appendText("URI was: "));
    }

    /**
     * URI path matcher.
     * @since 0.10
     */
    public static final class HasPath extends TypeSafeMatcher<URI> {

        /**
         * URI path matcher.
         */
        private final Matcher<String> target;

        /**
         * Match URI against expected path.
         * @param path Expected path
         */
        public HasPath(final String path) {
            this(new IsEqual<>(path));
        }

        /**
         * Match URI against path matcher.
         * @param target Path matcher
         */
        public HasPath(final Matcher<String> target) {
            this.target = target;
        }

        @Override
        public boolean matchesSafely(final URI item) {
            return this.target.matches(item.getPath());
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("path ").appendDescriptionOf(this.target);
        }

        @Override
        public void describeMismatchSafely(final URI item, final Description description) {
            description.appendText("path was: ").appendValue(item.getPath());
        }
    }
}
