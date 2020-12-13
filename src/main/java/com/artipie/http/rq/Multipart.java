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

import com.artipie.http.stream.ByteByByteSplit;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A multipart parser. Parses a Flow of Bytes into a flow of Multiparts
 * See
 * <a href="https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html">rfc1341</a>
 * spec.
 * @todo #32:60min Finish implementation.
 *  In order to ensure that multipart parser works correctly, the MultipartTest has been written.
 *  The test is disabled for now, but, when this class will be fully implemented, the test should be
 *  enable.
 * @since 0.4
 * @checkstyle ConstantUsageCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.UnusedPrivateField", "PMD.SingularField"})
public final class Multipart implements Processor<ByteBuffer, Part> {

    /**
     * The CRLF.
     */
    private static final String CRLF = "\r\n";

    /**
     * The subscriber part.
     */
    private final Subscriber<ByteBuffer> subscriber;

    /**
     * The publisher part.
     */
    private final Publisher<Part> publisher;

    /**
     * Ctor.
     *
     * @param headers Request headers.
     */
    public Multipart(final Iterable<Map.Entry<String, String>> headers) {
        this(() -> boundary(headers));
    }

    /**
     * Ctor.
     * @param boundary The boundary supplier.
     */
    public Multipart(final Supplier<String> boundary) {
        this(boundary.get());
    }

    /**
     * Ctor.
     *
     * @param boundary Multipart body boundary
     */
    public Multipart(final String boundary) {
        this(new ByteByByteSplit(boundary.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Ctor.
     * @param processor The processor.
     */
    public Multipart(final Processor<ByteBuffer, Publisher<ByteBuffer>> processor) {
        this.subscriber = processor;
        this.publisher = Flowable.fromPublisher(processor).map(body -> new PartFromPublisher(new ArrayList<>(), body));
    }

    @Override
    public void subscribe(final Subscriber<? super Part> sub) {
        this.publisher.subscribe(sub);
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        this.subscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(final ByteBuffer item) {
        this.subscriber.onNext(item);
    }

    @Override
    public void onError(final Throwable throwable) {
        this.subscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        this.subscriber.onComplete();
    }

    /**
     * Boundary from headers.
     *
     * @param headers Request headers.
     * @return Request boundary
     */
    private static String boundary(final Iterable<Map.Entry<String, String>> headers) {
        final Pattern pattern = Pattern.compile("boundary=(\\w+)");
        final String type = StreamSupport.stream(headers.spliterator(), false)
            .filter(header -> header.getKey().equalsIgnoreCase("content-type"))
            .map(Map.Entry::getValue)
            .findFirst()
            .get();
        final Matcher matcher = pattern.matcher(type);
        matcher.find();
        return matcher.group(1);
    }
}
