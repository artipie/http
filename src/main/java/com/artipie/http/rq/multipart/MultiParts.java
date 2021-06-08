/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rq.multipart;

import com.artipie.ArtipieException;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Multipart parts publisher.
 *
 * @since 1.0
 */
public final class MultiParts implements Processor<ByteBuffer, RqMultipart.Part>, ByteBufferTokenizer.Receiver {

    private final AtomicReference<Subscriber<? super RqMultipart.Part>> downstream = new AtomicReference<>();
    private final AtomicReference<Subscription> upstream = new AtomicReference<>();
    private final ByteBufferTokenizer tokenizer;
    private volatile MultiPart current;
    private final ExecutorService exec;

    /**
     * New multipart parts publisher for upstream publisher.
     */
    public MultiParts(final byte[] boundary, ExecutorService exec) {
        this.tokenizer = new ByteBufferTokenizer(this, boundary);
        this.exec = exec;
    }

    @Override
    public void subscribe(final Subscriber<? super RqMultipart.Part> sub) {
        if (this.downstream.compareAndSet(null, sub)) {
            sub.onSubscribe(upstream.get());
        } else {
            sub.onSubscribe(DummySubscription.VALUE);
            sub.onError(new IllegalStateException("Downstream already connected"));
            return;
        }
        if (upstream.get() != null) {
            this.upstream.get().request(1);
        }
    }

    @Override
    public void onSubscribe(final Subscription sub) {
        this.upstream.set(sub);
        if (downstream.get() != null) {
            sub.request(1);
        }
    }

    @Override
    public void onNext(final ByteBuffer chunk) {
        this.tokenizer.push(chunk);
    }

    @Override
    public void onError(final Throwable err) {
        this.downstream.getAndSet(null).onError(
                new ArtipieException("Upstream failed", err)
        );
    }

    @Override
    public void onComplete() {
        this.downstream.getAndSet(null).onComplete();
    }

    @Override
    public synchronized void receive(final ByteBuffer next, final boolean end) {
        if (this.current == null) {
            this.current = new MultiPart(this.upstream.get(), part -> this.downstream.get().onNext(part), exec);
        }
        this.current.push(next);
        if (end) {
            this.current.flush();
            this.current = null;
        }
    }
}
