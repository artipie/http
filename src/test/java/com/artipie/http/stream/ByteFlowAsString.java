/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.stream;

import com.artipie.asto.Remaining;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.reactivestreams.Publisher;

/**
 * Turn a flow of ByteBuffer's into a string.
 *
 * @since 0.1
 */
public class ByteFlowAsString {

    /**
     * The flow of ByteBuffer's.
     */
    private final Publisher<ByteBuffer> publisher;

    /**
     * Ctor.
     * @param split The split.
     */
    public ByteFlowAsString(final ByteByByteSplit split) {
        this(Flowable.fromPublisher(split).flatMap(pub -> pub));
    }

    /**
     * Ctor.
     * @param publisher The flow
     */
    public ByteFlowAsString(final Publisher<ByteBuffer> publisher) {
        this.publisher = publisher;
    }

    /**
     * Fetch stream elements and turn them into a single string.
     * @return The string
     */
    public String value() {
        return new String(
            Flowable.fromPublisher(this.publisher)
                .toList()
                .blockingGet()
                .stream()
                .map(byteBuffer -> new Remaining(byteBuffer).bytes())
                .reduce(
                    (one, another) -> {
                        final byte[] res = new byte[one.length + another.length];
                        System.arraycopy(one, 0, res, 0, one.length);
                        System.arraycopy(another, 0, res, one.length, another.length);
                        return res;
                    }
                )
                .get(),
            StandardCharsets.UTF_8
        );
    }
}
