package com.artipie.http.rq.multipart;

import java.util.function.LongUnaryOperator;

/**
 * New demand calculator.
 * It counts boundary and special cases with {@link Long#MAX_VALUE}.
 * @param amount New demand request
 * @return Operator to update current demand
 */
public class DemandAdder implements LongUnaryOperator {

    private static final LongUnaryOperator MAX = (old) -> Long.MAX_VALUE;

    private static final LongUnaryOperator MIN = (old) -> old;

    private final long amount;

    private DemandAdder(long amount) {
        this.amount = amount;
    }

    @Override
    public long applyAsLong(long old) {
        final long next;
        final long tmp = old + amount;
        if (tmp < 0) {
            next = Long.MAX_VALUE;
        } else {
            next = tmp;
        }
        return next;
    }

    public static LongUnaryOperator get(final long amount) {
        final LongUnaryOperator res;
        if (amount < 0) {
            throw new IllegalStateException("amount can't be less than zero");
        }
        if (amount == Long.MAX_VALUE) {
            return DemandAdder.MAX;
        }
        if (amount == 0) {
            return DemandAdder.MIN;
        }
        return new DemandAdder(amount);
    }
}
