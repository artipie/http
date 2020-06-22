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
import com.artipie.http.Response;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.IntStream;

/**
 * Group response.
 * <p>
 * The list of responses which can be send to connection by specified order.
 * </p>
 * @since 0.11
 */
final class GroupResponse implements Response {

    /**
     * Responses.
     */
    private final List<Response> responses;

    /**
     * New group response.
     * @param responses Responses to group
     */
    GroupResponse(final List<Response> responses) {
        this.responses = responses;
    }

    @Override
    public CompletionStage<Void> send(final Connection con) {
        final GroupResults results = new GroupResults(this.responses.size());
        return CompletableFuture.allOf(
            IntStream.range(0, this.responses.size())
                .mapToObj(
                    pos -> this.responses.get(pos)
                        .send(new GroupConnection(con, pos, results))
                        .toCompletableFuture()
                ).toArray(CompletableFuture<?>[]::new)
        );
    }
}
