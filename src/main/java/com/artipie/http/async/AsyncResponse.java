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
package com.artipie.http.async;

import com.artipie.http.Connection;
import com.artipie.http.Response;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import io.reactivex.Single;
import java.util.concurrent.CompletionStage;

/**
 * Async response from {@link CompletionStage}.
 * @since 0.6
 */
public final class AsyncResponse implements Response {

    /**
     * Source stage.
     */
    private final CompletionStage<? extends Response> future;

    /**
     * Response from {@link Single}.
     * @param single Single
     */
    public AsyncResponse(final Single<? extends Response> single) {
        this(single.to(SingleInterop.get()));
    }

    /**
     * Response from {@link CompletionStage}.
     * @param future Stage
     */
    public AsyncResponse(final CompletionStage<? extends Response> future) {
        this.future = future;
    }

    @Override
    public CompletionStage<Void> send(final Connection connection) {
        return this.future.thenCompose(rsp -> rsp.send(connection));
    }

    @Override
    public String toString() {
        return String.format(
            "(%s: %s)",
            this.getClass().getSimpleName(),
            this.future.toString()
        );
    }
}
