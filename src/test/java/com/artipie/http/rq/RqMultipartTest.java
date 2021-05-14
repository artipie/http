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
package com.artipie.http.rq;

import com.artipie.asto.Content;
import com.artipie.asto.ext.ContentAs;
import com.artipie.http.headers.ContentType;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for multipart request parser.
 * @since 1.0
 */
final class RqMultipartTest {

    @Test
    @Disabled
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    void processesSimpleMultipartRequest() throws Exception {
        final String first = String.join(
            "\n",
            "This is implicitly typed plain ASCII text.",
            "It does NOT end with a linebreak."
        );
        final String second = String.join(
            "\n",
            "This is explicitly typed plain ASCII text.",
            "It DOES end with a linebreak."
        );
        final String simple = String.join(
            "\n\r",
            String.join(
                "\n",
                "This is the preamble.  It is to be ignored, though it",
                "is a handy place for mail composers to include an",
                "explanatory note to non-MIME compliant readers."
            ),
            "--simple boundary",
            "",
            first,
            "--simple boundary",
            "Content-type: text/plain; charset=us-ascii",
            "",
            second,
            "--simple boundary--",
            "This is the epilogue.  It is also to be ignored."
        );
        final List<String> parsed = Flowable.fromPublisher(
            new RqMultipart(
                new ContentType("multipart/mixed; boundary=\"simple boundary\""),
                new Content.From(simple.getBytes(StandardCharsets.US_ASCII))
            ).parts()
        ).<String>flatMapSingle(
            part -> Single.just(part).to(ContentAs.STRING)
        ).toList().blockingGet();
        MatcherAssert.assertThat(
            parsed,
            Matchers.contains(first, second)
        );
    }
}
