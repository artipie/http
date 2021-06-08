/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rq.multipart;

import com.artipie.asto.ext.PublisherAs;
import com.artipie.http.hm.RqHasHeader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;

final class MultiPartTest {

    @Test
    void parsePart() throws Exception {
        final Flowable<ByteBuffer> upstream = Flowable.fromArray(
                "Content-l", "ength", ": 24\r\n",
                "Con", "tent-typ", "e: ", "appl", "ication/jso", "n\r\n\r\n{\"foo",
                "\": \"b", "ar\", ", "\"val\": [4]}\r\n"
        ).map(str -> ByteBuffer.wrap(str.getBytes()));
        final class Subscriber implements FlowableSubscriber<ByteBuffer> {

            private final CompletableFuture<RqMultipart.Part> future;

            private volatile MultiPart part;

            Subscriber(CompletableFuture<RqMultipart.Part> future) {
                this.future = future;
            }

            @Override
            public synchronized void onSubscribe(Subscription subscription) {
                this.part = new MultiPart(subscription, future::complete, Executors.newCachedThreadPool());
                subscription.request(1);
            }

            @Override
            public void onNext(final ByteBuffer buffer) {
                this.part.push(buffer);
            }

            @Override
            public void onError(final Throwable err) {
                this.future.completeExceptionally(err);
            }

            @Override
            public void onComplete() {
                this.part.flush();
            }
        }
        final CompletableFuture<RqMultipart.Part> future = new CompletableFuture<>();
        upstream.subscribe(new Subscriber(future));
        final RqMultipart.Part part = future.get();
        MatcherAssert.assertThat(
                part.headers(),
                Matchers.allOf(
                        new RqHasHeader("content-length", Matchers.contains("24")),
                        new RqHasHeader("content-type", Matchers.contains("application/json"))
                )
        );

        MatcherAssert.assertThat(
                new PublisherAs(part).string(StandardCharsets.US_ASCII).toCompletableFuture().get(),
                Matchers.equalTo("{\"foo\": \"bar\", \"val\": [4]}")
        );
    }
}