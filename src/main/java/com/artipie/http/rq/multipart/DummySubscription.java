package com.artipie.http.rq.multipart;

import org.reactivestreams.Subscription;

/**
 * Dummy subscription that do nothing.
 * It's a requirement of reactive-streams specification to
 * call {@code onSubscribe} on subscriber before any other call.
 */

enum  DummySubscription implements Subscription {
    VALUE;

    @Override
    public void request(long l) {

    }

    @Override
    public void cancel() {

    }
}
