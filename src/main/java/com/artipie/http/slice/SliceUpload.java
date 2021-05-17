/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.slice;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.reactivestreams.Publisher;

/**
 * Slice to upload the resource to storage by key from path.
 * @see SliceDownload
 * @since 0.6
 */
public final class SliceUpload implements Slice {

    /**
     * Storage.
     */
    private final Storage storage;

    /**
     * Path to key transformation.
     */
    private final Function<String, Key> transform;

    /**
     * Slice by key from storage.
     * @param storage Storage
     */
    public SliceUpload(final Storage storage) {
        this(storage, KeyFromPath::new);
    }

    /**
     * Slice by key from storage using custom URI path transformation.
     * @param storage Storage
     * @param transform Transformation
     */
    public SliceUpload(final Storage storage,
        final Function<String, Key> transform) {
        this.storage = storage;
        this.transform = transform;
    }

    @Override
    public Response response(final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        return new AsyncResponse(
            CompletableFuture.supplyAsync(() -> new RequestLineFrom(line).uri().getPath())
                .thenApply(this.transform)
                .thenCompose(key -> this.storage.save(key, new ContentWithSize(body, headers)))
                .thenApply(rsp -> new RsWithStatus(RsStatus.CREATED))
        );
    }
}
