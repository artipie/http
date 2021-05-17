/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.stream;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.ArrayUtils;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Byte stream split implementation based on Circular buffer of bytes.
 *
 * @todo #32:30min Cancellation support.
 *  For now downstream and down downstream cancellation is not supported,
 *  but we definitely need to have it.
 * @todo #32:30min Downstream emission subscription.
 *  We need to be aware of the fact that downstream switch doesn't happen instantly.
 *  For now, implementation does not rely on that.
 * @todo #32:30min Full reactive streams tck compatibility.
 *  The processor implementation should be verified by reactive streams tck in order to ensure
 *  specification compatibility.
 * @since 0.4
 * @checkstyle MemberNameCheck (500 lines)
 * @checkstyle LongVariable (500 lines)
 * @checkstyle TooManyMethods (500 lines)
 * @checkstyle EmptyLineSeparatorCheck (500 lines)
 */
@SuppressWarnings({
    "PMD.LongVariable",
    "PMD.TooManyMethods",
    "PMD.AvoidDuplicateLiterals"
})
public final class ByteByByteSplit implements Processor<ByteBuffer, Publisher<ByteBuffer>> {

    /**
     * A ring buffer with bytes. Used for delimiter findings.
     */
    private final CircularFifoQueue<Byte> ring;

    /**
     * The stream delimiter.
     */
    private final byte[] delim;

    /**
     * The splitted buffers. Empty means delimiter.
     */
    private final LinkedBlockingQueue<Optional<ByteBuffer>> storage;

    /**
     * The upstream to request elements from.
     */
    private final AtomicReference<Optional<Subscription>> upstream;

    /**
     * Downstream to emit elements to.
     */
    private final AtomicReference<Optional<Subscriber<? super Publisher<ByteBuffer>>>> downstream;

    /**
     * Downstream of a downstream element.
     */
    private final AtomicReference<Optional<Subscriber<? super ByteBuffer>>> downDownstream;

    /**
     * Is this processor already started?
     */
    private final AtomicBoolean started;

    /**
     * Has upstream been terminated.
     */
    private final AtomicBoolean upstreamTerminated;

    /**
     * The downstream demand.
     */
    private final AtomicLong downDemand;

    /**
     * The down downstream demand.
     */
    private final AtomicLong downDownDemand;

    /**
     * The object to sync on for upstream calls.
     */
    private final Object upSync;

    /**
     * The object to sync on for downstream calls.
     */
    private final Object downSync;

    /**
     * Ctor.
     *
     * @param delim The delimiter.
     */
    public ByteByByteSplit(final byte[] delim) {
        this.ring = new CircularFifoQueue<>(delim.length);
        this.delim = Arrays.copyOf(delim, delim.length);
        this.upstream = new AtomicReference<>(Optional.empty());
        this.downstream = new AtomicReference<>(Optional.empty());
        this.started = new AtomicBoolean(false);
        this.downDownstream = new AtomicReference<>(Optional.empty());
        this.storage = new LinkedBlockingQueue<>();
        this.upstreamTerminated = new AtomicBoolean(false);
        this.downDemand = new AtomicLong(0);
        this.downDownDemand = new AtomicLong(0);
        this.upSync = new Object();
        this.downSync = new Object();
    }

    @Override
    public void subscribe(final Subscriber<? super Publisher<ByteBuffer>> sub) {
        if (this.downstream.get().isPresent()) {
            throw new IllegalStateException("Only one subscription is allowed");
        }
        this.downstream.set(Optional.of(sub));
        sub.onSubscribe(
            new Subscription() {
                @Override
                public void request(final long ask) {
                    synchronized (ByteByByteSplit.this.downSync) {
                        ByteByByteSplit.this.downDemand.updateAndGet(operand -> operand + ask);
                        ByteByByteSplit.this.upstream.get().get().request(ask);
                    }
                }

                @Override
                public void cancel() {
                    synchronized (ByteByByteSplit.this.downSync) {
                        throw new IllegalStateException("Cancel is not allowed");
                    }
                }
            }
        );
        this.tryToStart();
    }

    @Override
    public void onSubscribe(final Subscription sub) {
        synchronized (this.upSync) {
            if (this.downstream.get().isPresent()) {
                throw new IllegalStateException("Only one subscription is allowed");
            }
            this.upstream.set(Optional.of(sub));
        }
    }

    @Override
    public void onNext(final ByteBuffer next) {
        synchronized (this.upSync) {
            final byte[] bytes = new byte[next.remaining()];
            next.get(bytes);
            ByteBuffer current = ByteByByteSplit.bufWithInitMark(bytes.length);
            for (final byte each : bytes) {
                final boolean eviction = this.ring.isAtFullCapacity();
                if (eviction) {
                    final Byte last = this.ring.get(0);
                    current.put(last);
                }
                this.ring.add(each);
                if (Arrays.equals(this.delim, this.ringBytes())) {
                    this.ring.clear();
                    current.limit(current.position());
                    current.reset();
                    this.emit(Optional.of(current));
                    this.emit(Optional.empty());
                    current = ByteByByteSplit.bufWithInitMark(bytes.length);
                }
            }
            current.limit(current.position());
            current.reset();
            this.emit(Optional.of(current));
        }
    }

    @Override
    public void onError(final Throwable throwable) {
        synchronized (this.upSync) {
            this.upstreamTerminated.set(true);
            this.downstream.get().ifPresent(value -> value.onError(throwable));
        }
    }

    @Override
    public void onComplete() {
        synchronized (this.upSync) {
            this.upstreamTerminated.set(true);
            this.emit(Optional.of(ByteBuffer.wrap(this.ringBytes())));
        }
    }

    /**
     * Create buffer with initial.
     *
     * @param size The buf size.
     * @return Buffer with the initial mark.
     */
    private static ByteBuffer bufWithInitMark(final int size) {
        final ByteBuffer current = ByteBuffer.allocate(size);
        current.mark();
        return current;
    }

    /**
     * Return currently held buffer bytes.
     * @return Currently held buffer bytes.
     */
    private byte[] ringBytes() {
        return ArrayUtils.toPrimitive(this.ring.stream().toArray(Byte[]::new));
    }

    /**
     * Try to start the processor.
     */
    private void tryToStart() {
        if (this.downstream.get().isPresent()
            && this.upstream.get().isPresent()
            && this.started.compareAndSet(false, true)) {
            this.emitNextSubSub();
        }
    }

    /**
     * Emit a buffer from the upstream.
     *
     * @param buffer Buffer or empty if a sub stream needs to be completed.
     */
    private void emit(final Optional<ByteBuffer> buffer) {
        this.storage.add(buffer);
        this.meetDemand();
    }

    /**
     * Attempt to meet the downstream demand.
     */
    private void meetDemand() {
        synchronized (this.downSync) {
            if (this.downDownstream.get().isPresent()) {
                while (this.downDownDemand.get() > 0 && this.storage.size() > 0) {
                    this.downDownDemand.decrementAndGet();
                    final Optional<ByteBuffer> poll = this.storage.poll();
                    if (poll.isPresent()) {
                        this.downDownstream.get().get().onNext(poll.get());
                    } else {
                        this.downDownstream.get().get().onComplete();
                        this.emitNextSubSub();
                    }
                }
                if (this.upstreamTerminated.get()) {
                    this.downDownstream.get().get().onComplete();
                    this.downstream.get().get().onComplete();
                }
            }
        }
    }

    /**
     * Emit next sub stream.
     */
    private void emitNextSubSub() {
        this.downstream.get().get().onNext(
            (Publisher<ByteBuffer>) sub -> {
                this.downDownstream.set(Optional.of(sub));
                sub.onSubscribe(
                    new Subscription() {
                        @Override
                        public void request(final long requested) {
                            ByteByByteSplit.this.downDownDemand.updateAndGet(
                                operand -> operand + requested
                            );
                            ByteByByteSplit.this.meetDemand();
                        }

                        @Override
                        @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
                        public void cancel() {
                        }
                    }
                );
            }
        );
    }
}
