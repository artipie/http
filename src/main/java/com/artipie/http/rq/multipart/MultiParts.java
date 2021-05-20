/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rq.multipart;

import com.artipie.http.Headers;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongUnaryOperator;
import org.apache.commons.lang3.NotImplementedException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Multipart parts publisher.
 * @since 1.0
 */
public final class MultiParts implements Publisher<RqMultipart.Part> {

    /**
     * Upstream processor.
     */
    private final Worker worker;

    /**
     * New multipart parts publisher for upstream publisher.
     * @param upstream Publisher
     * @param exec Executor service for processing
     */
    public MultiParts(final Publisher<ByteBuffer> upstream, final ExecutorService exec) {
        this.worker = new Worker(upstream, exec);
    }

    @Override
    public void subscribe(final Subscriber<? super RqMultipart.Part> subscriber) {
        this.worker.attach(subscriber);
    }

    /**
     * Worker runnable for multipart upstream chunks processing.
     * @since 1.0
     */
    private static final class Worker implements Runnable, Subscription {

        /**
         * Dummy subscription that do nothing.
         * It's a requirement of reactive-streams specification to
         * call {@code onSubscribe} on subscriber before any other call.
         */
        private static final Subscription SUB_DUMMY = new Subscription() {

            @Override
            public void request(final long req) {
                // do nothing
            }

            @Override
            public void cancel() {
                // do nothing
            }
        };

        /**
         * Upstream publisher.
         */
        private final Publisher<ByteBuffer> upstream;

        /**
         * Executor service.
         */
        private final ExecutorService exec;

        /**
         * Running flag.
         */
        private final AtomicBoolean running;

        /**
         * Demand counter.
         */
        private final AtomicLong demand;

        /**
         * Current downstream publisher reference.
         */
        private final AtomicReference<Subscriber<? super RqMultipart.Part>> downstream;

        /**
         * New worker for upstream.
         * @param upstream Publisher
         * @param exec Executor service
         */
        Worker(final Publisher<ByteBuffer> upstream, final ExecutorService exec) {
            this.upstream = upstream;
            this.exec = exec;
            this.running = new AtomicBoolean();
            this.demand = new AtomicLong();
            this.downstream = new AtomicReference<>();
        }

        @Override
        public void run() {
            throw new NotImplementedException(
                String.format("Worker run is not implemented (%s)", this.upstream)
            );
        }

        @Override
        public void request(final long amount) {
            this.demand.updateAndGet(Worker.addNewDemand(amount));
            if (!this.running.compareAndSet(false, true)) {
                this.exec.submit(this);
            }
        }

        @Override
        public void cancel() {
            throw new NotImplementedException(
                String.format("Worker cancel is not implemented (%s)", this.upstream)
            );
        }

        /**
         * Attach downstream and notify subscriber.
         * @param sub Publisher
         */
        public void attach(final Subscriber<? super RqMultipart.Part> sub) {
            if (this.downstream.compareAndSet(null, sub)) {
                sub.onSubscribe(this);
            } else {
                sub.onSubscribe(Worker.SUB_DUMMY);
                sub.onError(new IllegalStateException("Downstream already connected"));
            }
        }

        /**
         * New demand calculator.
         * It counts boundary and special cases with {@link Long#MAX_VALUE}.
         * @param amount New demand request
         * @return Operator to update current demand
         */
        private static LongUnaryOperator addNewDemand(final long amount) {
            return (final long old) -> {
                final long next;
                if (old == Long.MAX_VALUE) {
                    next = old;
                } else if (amount == Long.MAX_VALUE) {
                    next = amount;
                } else {
                    final long tmp = old + amount;
                    if (tmp < 0) {
                        next = Long.MAX_VALUE;
                    } else {
                        next = tmp;
                    }
                }
                return next;
            };
        }
    }

    /**
     * Part of multipart.
     *
     * @since 1.0
     */
    public static final class PartPublisher implements Publisher<ByteBuffer> {

        /**
         * Part headers.
         */
        private final Headers hdr;

        /**
         * Part body.
         */
        private final Publisher<ByteBuffer> source;

        /**
         * New part.
         *
         * @param headers Part headers
         * @param source Part body
         */
        public PartPublisher(final Headers headers, final Publisher<ByteBuffer> source) {
            this.hdr = headers;
            this.source = source;
        }

        @Override
        public void subscribe(final Subscriber<? super ByteBuffer> sub) {
            this.source.subscribe(sub);
        }

        /**
         * Part headers.
         *
         * @return Headers
         */
        public Headers headers() {
            return this.hdr;
        }
    }
}
