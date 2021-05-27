/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rq.multipart;

import java.nio.ByteBuffer;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@lint BufAccumulator}.
 * @since 1.0
 * @checkstyle MagicNumberCheck (500 lines)
 */
final class BufAccumulatorTest {

    @Test
    void concatChunks() {
        try (BufAccumulator acc = new BufAccumulator(1)) {
            acc.push(ByteBuffer.wrap("Hello ".getBytes()))
                .push(ByteBuffer.wrap("chunk".getBytes()))
                .push(ByteBuffer.wrap("!".getBytes()));
            MatcherAssert.assertThat(new String(acc.array()), new IsEqual<>("Hello chunk!"));
        }
    }

    @Test
    void dropBytes() {
        try (BufAccumulator acc = new BufAccumulator(1)) {
            acc.push(ByteBuffer.wrap(new byte[]{0, 1, 2, 3}));
            acc.drop(2);
            MatcherAssert.assertThat(acc.array(), new IsEqual<>(new byte[]{2, 3}));
        }
    }
}
