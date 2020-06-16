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
package com.artipie.http.rt;

import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rq.RqMethod;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.cactoos.list.ListOf;

/**
 * Routing rule.
 * <p>
 * A rule which is applied to the request metadata such as request line and
 * headers. If rule matched, then routing slice {@link SliceRoute} will
 * redirect request to target {@link com.artipie.http.Slice}.
 * </p>
 * @since 0.5
 */
public interface RtRule {

    /**
     * Fallback RtRule.
     */
    RtRule FALLBACK = (line, headers) -> true;

    /**
     * Apply this rule to request.
     * @param line Request line
     * @param headers Request headers
     * @return True if rule passed
     */
    boolean apply(String line, Iterable<Map.Entry<String, String>> headers);

    /**
     * This rule is matched only when all of the rules are matched.
     * This class is kept for backward compatibility reasons.
     * @since 0.5
     * @deprecated use {@link All} instead
     */
    @Deprecated
    final class Multiple extends All {

        /**
         * Ctor.
         * @param rules Rules array
         */
        public Multiple(final RtRule... rules) {
            super(new ListOf<>(rules));
        }

        /**
         * Ctor.
         * @param rules Rules
         */
        public Multiple(final Iterable<RtRule> rules) {
            super(rules);
        }

    }

    /**
     * This rule is matched only when all of the rules are matched.
     * @since 0.10
     */
    class All implements RtRule {

        /**
         * Rules.
         */
        private final Iterable<RtRule> rules;

        /**
         * Route by multiple rules.
         * @param rules Rules array
         */
        public All(final RtRule... rules) {
            this(new ListOf<>(rules));
        }

        /**
         * Route by multiple rules.
         * @param rules Rules
         */
        public All(final Iterable<RtRule> rules) {
            this.rules = rules;
        }

        @Override
        public boolean apply(final String line,
            final Iterable<Map.Entry<String, String>> headers) {
            boolean match = true;
            for (final RtRule rule : this.rules) {
                if (!rule.apply(line, headers)) {
                    match = false;
                    break;
                }
            }
            return match;
        }
    }

    /**
     * This rule is matched only when any of the rules is matched.
     * @since 0.10
     */
    final class Any implements RtRule {

        /**
         * Rules.
         */
        private final Iterable<RtRule> rules;

        /**
         * Route by any of the rules.
         * @param rules Rules array
         */
        public Any(final RtRule... rules) {
            this(new ListOf<>(rules));
        }

        /**
         * Route by any of the rules.
         * @param rules Rules
         */
        public Any(final Iterable<RtRule> rules) {
            this.rules = rules;
        }

        @Override
        public boolean apply(final String line,
            final Iterable<Map.Entry<String, String>> headers) {
            boolean match = false;
            for (final RtRule rule : this.rules) {
                if (rule.apply(line, headers)) {
                    match = true;
                    break;
                }
            }
            return match;
        }
    }

    /**
     * Route by method.
     * @since 0.5
     */
    final class ByMethod implements RtRule {

        /**
         * Method name.
         */
        private final RqMethod method;

        /**
         * Route by method.
         * @param method Method name
         */
        public ByMethod(final RqMethod method) {
            this.method = method;
        }

        @Override
        public boolean apply(final String line,
            final Iterable<Map.Entry<String, String>> headers) {
            return Objects.equals(new RequestLineFrom(line).method(), this.method);
        }
    }

    /**
     * Route by path.
     * @since 0.5
     */
    final class ByPath implements RtRule {

        /**
         * Request URI path pattern.
         */
        private final Pattern ptn;

        /**
         * By path rule.
         * @param ptn Path pattern string
         */
        public ByPath(final String ptn) {
            this(Pattern.compile(ptn));
        }

        /**
         * By path rule.
         * @param ptn Path pattern
         */
        public ByPath(final Pattern ptn) {
            this.ptn = ptn;
        }

        @Override
        public boolean apply(final String line,
            final Iterable<Map.Entry<String, String>> headers) {
            return this.ptn.matcher(
                new RequestLineFrom(line).uri().getPath()
            ).matches();
        }
    }
}
