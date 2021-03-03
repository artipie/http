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

import com.artipie.http.Headers;
import com.artipie.http.Slice;
import com.artipie.http.headers.Header;
import com.artipie.http.rq.RequestLine;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cqfn.rio.Buffers;
import org.cqfn.rio.stream.ReactiveInputStream;

/**
 * Slice wrapper for using in servlet API.
 * @since 0.18
 */
public final class ServletSliceWrap {

    /**
     * Target slice.
     */
    private final Slice target;

    /**
     * Wraps {@link Slice} to provide methods for servlet API.
     * @param target Slice
     */
    public ServletSliceWrap(final Slice target) {
        this.target = target;
    }

    /**
     * Handler with async context.
     * @param ctx Servlet async context
     */
    public void handle(final AsyncContext ctx) {
        this.handle((HttpServletRequest) ctx.getRequest(), (HttpServletResponse) ctx.getResponse())
            .handle(
                (success, error) -> {
                    if (error != null) {
                        Logger.error(this, "Failed to process async request: %[exception]s", error);
                    }
                    ctx.complete();
                    return success;
                }
            );
    }

    /**
     * Handle servlet request.
     * @param req Servlet request
     * @param rsp Servlet response
     * @return Future
     * @checkstyle ReturnCountCheck (10 lines)
     */
    @SuppressWarnings("PMD.OnlyOneReturn")
    public CompletionStage<Void> handle(final HttpServletRequest req,
        final HttpServletResponse rsp) {
        try {
            return this.target.response(
                new RequestLine(req.getMethod(), req.getRequestURI(), req.getProtocol()).toString(),
                ServletSliceWrap.headers(req),
                new ReactiveInputStream(req.getInputStream()).read(Buffers.Standard.K8)
            ).send(new ServletConnection(rsp));
        } catch (final IOException iex) {
            final CompletableFuture<Void> failure = new CompletableFuture<>();
            failure.completeExceptionally(new CompletionException(iex));
            return failure;
        }
    }

    /**
     * Artipie request headers from servlet request.
     * @param req Servlet request
     * @return Artipie headers
     */
    private static Headers headers(final HttpServletRequest req) {
        return new Headers.From(
            Collections.list(req.getHeaderNames()).stream().flatMap(
                name -> Collections.list(req.getHeaders(name)).stream()
                    .map(val -> new Header(name, val))
            ).collect(Collectors.toList())
        );
    }
}
