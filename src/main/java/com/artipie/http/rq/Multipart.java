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

import com.artipie.http.headers.Header;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A multipart parser. Parses a Flow of Bytes into a flow of Multiparts
 * See
 * <a href="https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html">rfc1341</a>
 * spec.
 * @since 0.4
 * @checkstyle ConstantUsageCheck (500 lines)
 * @checkstyle StringLiteralsConcatenationCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.UnusedPrivateField", "PMD.SingularField"})
public final class Multipart implements Processor<ByteBuffer, Part> {

    /**
     * The CRLF.
     */
    private static final String CRLF = "\r\n";

    /**
     * The subscriber.
     */
    private final AtomicReference<Subscriber<? super Part>> dsub;

    /**
     * Is upstream publisher completed?
     */
    private final AtomicBoolean completed;

    /**
     * The buffers.
     */
    private final List<ByteBuffer> buffers;

    /**
     * The multipart boundary.
     */
    private String boundary;

    /**
     * Ctor.
     *
     * @param headers Request headers.
     */
    public Multipart(final Iterable<Map.Entry<String, String>> headers) {
        this(() -> boundaryFromHeaders(headers));
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
        this.boundary = boundary;
        this.buffers = new LinkedList<>();
        this.dsub = new AtomicReference<>(null);
        this.completed = new AtomicBoolean(false);
    }

    @Override
    public void subscribe(final Subscriber<? super Part> sub) {
        if (!this.dsub.compareAndSet(null, sub)) {
            throw new IllegalStateException("Only one subscription is allowed");
        }
        this.justParse();
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(final ByteBuffer item) {
        this.buffers.add(item);
    }

    @Override
    public void onError(final Throwable throwable) {
        throw new IllegalStateException(throwable);
    }

    @Override
    public void onComplete() {
        this.completed.set(true);
        this.justParse();
    }

    /**
     * Parse multipart stored at buffers.
     */
    private void justParse() {
        final Subscriber<? super Part> sub = this.dsub.get();
        if (this.completed.get() && sub != null) {
            final ByteBuffer resulted = ByteBuffer.allocate(
                this.buffers.stream().map(ByteBuffer::remaining).reduce(Integer::sum).orElse(0)
            );
            this.buffers.forEach(resulted::put);
            resulted.flip();
            final byte[] dst = new byte[resulted.remaining()];
            resulted.get(dst);
            final String str = new String(dst, StandardCharsets.UTF_8);
            final String main = str.split("--" + this.boundary + "--" + Multipart.CRLF)[0];
            final String[] split = main.split("--" + this.boundary);
            Flowable.fromArray(split)
                .filter(part -> !part.isEmpty())
                    .map(
                        part -> {
                            final String[] hnb = part.split(
                            Multipart.CRLF + Multipart.CRLF
                            );
                            final Iterable<Map.Entry<String, String>> headers =
                                Arrays.stream(hnb[0].split(Multipart.CRLF))
                                    .filter(p -> !p.isEmpty())
                                        .map(
                                            header -> {
                                                final String[] hdr = header.split(": ");
                                                return new Header(hdr[0], hdr[1]);
                                            }).collect(Collectors.toList());
                            final String body = hnb[1];
                            return new PartFromPublisher(
                                headers,
                                Flowable.just(
                                    ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8))
                                )
                            );
                        })
                .subscribe(sub);
        }
    }

    /**
     * Boundary from headers.
     *
     * @param headers Request headers.
     * @return Request boundary
     */
    private static String boundaryFromHeaders(final Iterable<Map.Entry<String, String>> headers) {
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
