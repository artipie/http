/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.slice;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.cactoos.map.MapEntry;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SliceDownload}.
 *
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

    @Test
    void downloadsByKeyFromPathAndHasProperHeader() throws Exception {
        final Storage storage = new InMemoryStorage();
        final String path = "some/path/target.txt";
        final byte[] data = "goodbye".getBytes(StandardCharsets.UTF_8);
        storage.save(new Key.From(path), new Content.From(data)).get();
        MatcherAssert.assertThat(
            new SliceDownload(storage).response(
                get(path),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new RsHasHeaders(
                new MapEntry<>("Content-Length", "7"),
                new MapEntry<>("Content-Disposition", "attachment; filename=\"target.txt\"")
            )
        );
    }

    private static String get(final String path) {
        return new RequestLine("GET", path, "HTTP/1.1").toString();
    }
}
