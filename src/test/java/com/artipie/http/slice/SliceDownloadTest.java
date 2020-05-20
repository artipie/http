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
package com.artipie.http.slice;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SliceDownload}.
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class SliceDownloadTest {

    @Test
    void downloadsByKeyFromPath() throws Exception {
        final Storage storage = new InMemoryStorage();
        final String path = "one/two/target.txt";
        final byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        storage.save(new Key.From(path), new Content.From(data)).get();
        MatcherAssert.assertThat(
            new SliceDownload(storage).response(
                get("/one/two/target.txt"), Collections.emptyList(), Flowable.empty()
            ),
            new RsHasBody(data)
        );
    }

    @Test
    void returnsNotFoundIfKeyDoesntExist() throws Exception {
        MatcherAssert.assertThat(
            new SliceDownload(new InMemoryStorage()).response(
                get("/not-exists"), Collections.emptyList(), Flowable.empty()
            ),
            new RsHasStatus(RsStatus.NOT_FOUND)
        );
    }

    @Test
    void returnsOkOnEmptyValue() throws Exception {
        final Storage storage = new InMemoryStorage();
        final String path = "empty.txt";
        final byte[] body = new byte[0];
        storage.save(new Key.From(path), new Content.From(body)).get();
        MatcherAssert.assertThat(
            new SliceDownload(storage).response(
                get("/empty.txt"), Collections.emptyList(), Flowable.empty()
            ),
            new ResponseMatcher(body)
        );
    }

    private static String get(final String path) {
        return new RequestLine("GET", path, "HTTP/1.1").toString();
    }
}
