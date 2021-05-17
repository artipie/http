/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */

package com.artipie.http.slice;

import com.artipie.asto.Storage;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.StandardRs;
import java.nio.ByteBuffer;
import java.util.Map;
import org.reactivestreams.Publisher;

/**
 * Delete decorator for Slice.
 *
 * @since 0.16
 */
public final class SliceDelete implements Slice {

    /**
     * Storage.
     */
    private final Storage storage;

    /**
     * Constructor.
     * @param storage Storage.
     */
    public SliceDelete(final Storage storage) {
        this.storage = storage;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        final KeyFromPath key = new KeyFromPath(new RequestLineFrom(line).uri().getPath());
        return new AsyncResponse(
            this.storage.exists(key).thenApply(
                exists -> {
                    final Response rsp;
                    if (exists) {
                        rsp = new AsyncResponse(
                            this.storage.delete(key).thenApply(none -> StandardRs.NO_CONTENT)
                        );
                    } else {
                        rsp = StandardRs.NOT_FOUND;
                    }
                    return rsp;
                }
            )
        );
    }
}
