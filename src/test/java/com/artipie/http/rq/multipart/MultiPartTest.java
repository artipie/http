/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rq.multipart;

import com.artipie.asto.ext.PublisherAs;
import io.reactivex.internal.functions.Functions;
import io.reactivex.subjects.SingleSubject;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link MultiPart}.
 *
 * @since 1.0
 */
final class MultiPartTest {

    @Test
    void parsePart() throws Exception {
        final SingleSubject<RqMultipart.Part> subj = SingleSubject.create();
        final MultiPart part = new MultiPart(Completion.FAKE, subj::onSuccess);
        Executors.newCachedThreadPool().submit(
            () -> {
                for (final String chunk : Arrays.asList(
                    "Content-l", "ength", ": 24\r\n",
                    "Con", "tent-typ", "e: ", "appl", "ication/jso", "n\r\n\r\n{\"foo",
                    "\": \"b", "ar\", ", "\"val\": [4]}"
                )) {
                    part.push(ByteBuffer.wrap(chunk.getBytes(StandardCharsets.US_ASCII)));
                    try {
                        // @checkstyle MagicNumberCheck (1 line)
                        Thread.sleep(100L);
                    } catch (final InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                        part.cancel();
                        return;
                    }
                }
                part.flush();
            }
        );
        MatcherAssert.assertThat(
            new PublisherAs(subj.flatMapPublisher(Functions.identity()))
                .string(StandardCharsets.US_ASCII)
                .toCompletableFuture().get(),
            Matchers.equalTo("{\"foo\": \"bar\", \"val\": [4]}")
        );
    }
}
