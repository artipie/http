/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rq.multipart;

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
//    @Disabled
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
            "\r\n",
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
