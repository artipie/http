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
package com.artipie.http.group;

import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.rs.RsStatus;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * One remote target connection.
 *
 * @since 0.11
 */
final class GroupConnection implements Connection {

    /**
     * Origin connection.
     */
    private final Connection origin;

    /**
     * Target order.
     */
    private final int pos;

    /**
     * Response results.
     */
    private final GroupResults results;

    /**
     * New connection for one target.
     * @param origin Origin connection
     * @param pos Order
     * @param results Results
     */
    GroupConnection(final Connection origin, final int pos, final GroupResults results) {
        this.origin = origin;
        this.pos = pos;
        this.results = results;
    }

    @Override
    public CompletionStage<Void> accept(final RsStatus status, final Headers headers,
        final Publisher<ByteBuffer> body) {
        synchronized (this.results) {
            return this.results.complete(
                this.pos, new GroupResult(status, headers, body), this.origin
            );
        }
    }
}
