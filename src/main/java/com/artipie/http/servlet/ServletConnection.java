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

package com.artipie.http.servlet;

import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.rs.RsStatus;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import javax.servlet.http.HttpServletResponse;
import org.cqfn.rio.WriteGreed;
import org.cqfn.rio.stream.ReactiveOutputStream;
import org.reactivestreams.Publisher;

/**
 * Connection implementation with servlet response as a back-end.
 * @since 0.18
 */
final class ServletConnection implements Connection {

    /**
     * Servlet response.
     */
    private final HttpServletResponse rsp;

    /**
     * New Artipie connection with servlet response back-end.
     * @param rsp Servlet response
     */
    ServletConnection(final HttpServletResponse rsp) {
        this.rsp = rsp;
    }

    // @checkstyle ReturnCountCheck (10 lines)
    @Override
    @SuppressWarnings("PMD.OnlyOneReturn")
    public CompletionStage<Void> accept(final RsStatus status,
        final Headers headers, final Publisher<ByteBuffer> body) {
        this.rsp.setStatus(Integer.parseInt(status.code()));
        headers.forEach(kv -> this.rsp.setHeader(kv.getKey(), kv.getValue()));
        try {
            return new ReactiveOutputStream(this.rsp.getOutputStream())
                .write(body, WriteGreed.SYSTEM.adaptive());
        } catch (final IOException iex) {
            final CompletableFuture<Void> failure = new CompletableFuture<>();
            failure.completeExceptionally(new CompletionException(iex));
            return failure;
        }
    }
}
