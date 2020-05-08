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
package com.artipie.http.rs;

import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasStatus;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RsWithBody}.
 * @since 0.9
 */
final class RsWithBodyTest {

    @Test
    void createsResponseWithStatusOkAndBody() {
        final byte[] body = "abc".getBytes();
        MatcherAssert.assertThat(
            new RsWithBody(ByteBuffer.wrap(body)),
            Matchers.allOf(
                new RsHasBody(body),
                new RsHasStatus(RsStatus.OK)
            )
        );
    }

    @Test
    void appendsBody() {
        final String body = "def";
        MatcherAssert.assertThat(
            new RsWithBody(new RsWithStatus(RsStatus.CREATED), body, StandardCharsets.UTF_8),
            Matchers.allOf(
                new RsHasBody(body.getBytes()),
                new RsHasStatus(RsStatus.CREATED)
            )
        );
    }

}
