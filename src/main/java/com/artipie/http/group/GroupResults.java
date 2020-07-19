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
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Group response results aggregator.
 * <p>This class is not thread safe.</p>
 * @since 0.11
 */
final class GroupResults {

    /**
     * List of results.
     */
    private final List<GroupResult> list;

    /**
     * Done flag.
     */
    private final AtomicBoolean done;

    /**
     * New results aggregator.
     * @param cap Capacity
     */
    GroupResults(final int cap) {
        this(new ArrayList<>(Collections.nCopies(cap, null)));
    }

    /**
     * Primary constructor.
     * @param list List of results
     */
    private GroupResults(final List<GroupResult> list) {
        this.list = list;
        this.done = new AtomicBoolean();
    }

    /**
     * Complete results.
     * <p>
     * This method checks if the response can be completed. If the result was succeed and
     * all previous ordered results were completed and failed, then the whole response will
     * be replied to the {@link Connection}. If any previous results is not completed, then
     * this result will be placed in the list to wait all previous results.
     * </p>
     * @param order Order of result
     * @param result Repayable result
     * @param con Connection to use for replay
     * @return Future
     * @checkstyle ReturnCountCheck (25 lines)
     */
    @SuppressWarnings("PMD.OnlyOneReturn")
    public CompletionStage<Void> complete(final int order, final GroupResult result,
        final Connection con) {
        if (order >= this.list.size()) {
            throw new IllegalStateException("Wrong order of result");
        }
        if (this.done.get()) {
            result.cancel();
            return CompletableFuture.completedFuture(null);
        }
        this.list.set(order, result);
        for (int pos = 0; pos < this.list.size(); ++pos) {
            final GroupResult target = this.list.get(pos);
            if (target == null) {
                return CompletableFuture.completedFuture(null);
            }
            if (target.success()) {
                this.done.set(true);
                this.list.remove(target);
                this.list.stream().filter(Objects::nonNull).forEach(GroupResult::cancel);
                return target.replay(con);
            }
        }
        this.done.set(true);
        return new RsWithStatus(RsStatus.NOT_FOUND).send(con);
    }
}
