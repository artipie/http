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
package com.artipie.http.stream;

import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ByteByByteSplit}.
 *
 * @since 0.4
 * @checkstyle MagicNumberCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ByteByByteSplitTest {

    @Test
    public void httpSplitWorks() {
        String mbody = "--d398737b067c2e88\n" +
            "content-disposition: form-data; name=\"hello\"; filename=\"text.txt\"\n" +
            "content-length: 17\n" +
            "content-type: text/plain; charset=UTF-8\n" +
            "\n" +
            "Hello worrrrld!!!\n" +
            "--d398737b067c2e88--\n123";
        System.out.println(mbody.length());
        final ByteByByteSplit split = new ByteByByteSplit("--d398737b067c2e88--\n".getBytes());
        this.buffersOfOneByteFlow(mbody).subscribe(split);
        AtomicLong al = new AtomicLong(0);
        Flowable<ByteBuffer> byteBufferFlowable = Flowable.fromPublisher(split).flatMap(mp -> {
            System.out.println("al" + al.incrementAndGet());
            return mp;
        });
        List<ByteBuffer> lists = Flowable.fromPublisher(byteBufferFlowable).toList().blockingGet();

            System.out.println("ls  " + lists.size());
            System.out.println(new ByteFlowAsString(Flowable.fromIterable(lists)).value());

//        MatcherAssert.assertThat(
//            new ByteFlowAsString(split).value(),
//            new IsEqual<>("--d398737b067c2e88\n" +
//                "content-disposition: form-data; name=\"hello\"; filename=\"text.txt\"\n" +
//                "content-length: 17\n" +
//                "content-type: text/plain; charset=UTF-8\n" +
//                "\n" +
//                "Hello worrrrld!!!\n123"
//            )
//        );
    }

    @Test
    public void basicSplitWorks() {
        final ByteByByteSplit split = new ByteByByteSplit(" ".getBytes());
        this.buffersOfOneByteFlow("how are you").subscribe(split);
        MatcherAssert.assertThat(
            new ByteFlowAsString(split).value(),
            new IsEqual<>("howareyou")
        );
    }

    @Test
    public void severalCharSplitWorks() {
        final ByteByByteSplit split = new ByteByByteSplit("__".getBytes());
        this.buffersOfOneByteFlow("how__are__you").subscribe(split);
        MatcherAssert.assertThat(
            new ByteFlowAsString(split).value(),
            new IsEqual<>("howareyou")
        );
    }

    private Flowable<ByteBuffer> buffersOfOneByteFlow(final String str) {
        return Flowable.fromArray(
            Arrays.stream(
                ArrayUtils.toObject(str.getBytes())
            ).map(
                (Byte aByte) -> {
                    final byte[] bytes = new byte[1];
                    bytes[0] = aByte;
                    return ByteBuffer.wrap(bytes);
                }
            ).toArray(ByteBuffer[]::new)
        );
    }
}
