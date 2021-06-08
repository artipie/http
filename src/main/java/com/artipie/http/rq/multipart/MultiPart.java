/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rq.multipart;

import com.artipie.http.Headers;
import org.reactivestreams.Subscriber;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.reactivestreams.Subscription;

/**
 * Multipart request part.
 * @since 1.0
 */
final class MultiPart implements RqMultipart.Part, ByteBufferTokenizer.Receiver, Subscription {

    /**
     * Header buffer capacity.
     */
    private static final int CAP_HEADER = 256;

    /**
     * Part body buffer capacity.
     */
    private static final int CAP_PART = 1024;

    /**
     * Delimiter token.
     */
    private static final String DELIM = "\r\n\r\n";

    /**
     * Header state flag.
     */
    private static final int STATE_HEADER = 1;

    /**
     * Body state flag.
     */
    private static final int STATE_BODY = 2;

    /**
     * Upstream subscription.
     */
    private final Subscription upstream;

    /**
     * CRLF tokenizer.
     */
    private final ByteBufferTokenizer tokenizer;

    /**
     * Multipart header.
     */
    private final MultipartHeaders hdr;

    /**
     * Downstream reference.
     */
    private final AtomicReference<Subscriber<? super ByteBuffer>> downstream;

    /**
     * Current state.
     */
    private final AtomicInteger state;

    /**
     * Ready callback.
     * <p>
     * Called when the header is received and the part could be submitted to
     * {@link com.artipie.http.rq.multipart.RqMultipart.Part} downstream.
     * </p>
     */
    private final Consumer<? super RqMultipart.Part> ready;

    /**
     * Temporary body accumulator.
     * <p>
     * It's needed when the downstream connected after the part of body received.
     * It may happen if the first chunk of body received with last header chunk
     * before downstream subscription.
     * </p>
     */
    private final BufAccumulator tmpacc;

    /**
     * Async back-pressure executor.
     */
    private final ExecutorService exec;

    /**
     * Completed flag.
     */
    private final AtomicBoolean completed;

    /**
     * New multipart request part.
     * @param upstream Upstream subscription
     * @param ready Ready callback
     * @param exec Back-pressure async executor
     */
    public MultiPart(Subscription upstream, Consumer<? super RqMultipart.Part> ready, ExecutorService exec) {
        this.upstream = upstream;
        this.ready = ready;
        this.exec = exec;
        this.tokenizer = new ByteBufferTokenizer(this, DELIM.getBytes(), CAP_PART);
        this.hdr = new MultipartHeaders(CAP_HEADER);
        this.downstream = new AtomicReference<>();
        this.state = new AtomicInteger(STATE_HEADER);
        this.tmpacc = new BufAccumulator(256);
        this.completed = new AtomicBoolean();
    }

    @Override
    public Headers headers() {
        return this.hdr;
    }

    @Override
    public synchronized void subscribe(Subscriber<? super ByteBuffer> sub) {
        if (this.downstream.get() != null) {
            sub.onSubscribe(DummySubscription.VALUE);
            sub.onError(new IllegalStateException("Downstream already connected"));
            return;
        }
        sub.onSubscribe(this);
        final ByteBuffer acc = this.tmpacc.duplicate();
        acc.rewind();
        if (acc.hasRemaining()) {
            sub.onNext(acc);
        }
        this.tmpacc.close();
        if (this.completed.get()) {
            sub.onComplete();
        } else {
            this.downstream.set(sub);
        }
    }

    /**
     * Push next chunk of raw data.
     * @param chunk Chunk buffer
     */
    void push(final ByteBuffer chunk) {
        this.tokenizer.push(chunk);
    }

    /**
     * Flush all data in temporary buffers.
     */
    void flush() {
        this.push(ByteBuffer.wrap(DELIM.getBytes()));
    }

    @Override
    public synchronized void receive(final ByteBuffer next, final boolean end) {
        final int current = state.get();
        switch (current) {
            case STATE_HEADER:
                this.hdr.push(next);
                this.upstream.request(1);
                break;
            case STATE_BODY:
                final Subscriber<? super ByteBuffer> subscriber = this.downstream.get();
                if (subscriber == null) {
                    this.tmpacc.push(next);
                } else {
                    subscriber.onNext(next);
                }
                break;
        }
        if (end && current == STATE_HEADER) {
            this.state.incrementAndGet();
            this.ready.accept(this);
        }
        if (end && current == STATE_BODY && this.completed.compareAndSet(false, true)) {
            final Subscriber<? super ByteBuffer> sub = this.downstream.getAndSet(null);
            if (sub != null) {
                sub.onComplete();
            }
        }
    }

    @Override
    public void request(final long amt) {
        this.exec.submit(() -> this.upstream.request(amt));
    }

    @Override
    public void cancel() {
        this.downstream.set(null);
        this.upstream.cancel();
    }
}
