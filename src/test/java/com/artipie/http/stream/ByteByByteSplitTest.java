/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.stream;

import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
