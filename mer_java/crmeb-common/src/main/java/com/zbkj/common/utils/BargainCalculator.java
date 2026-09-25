package com.zbkj.common.utils;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/** Cent arithmetic guarantees a positive cut and an exact final cut. Self-cut counts as one. */
public final class BargainCalculator {
    private BargainCalculator() { }
    public static BigDecimal cut(BigDecimal current, BigDecimal floor, int cutsLeft) {
        long cents = current.subtract(floor).movePointRight(2).longValueExact();
        if (cutsLeft < 1 || cents < cutsLeft) throw new IllegalArgumentException("Invalid bargain remainder");
        if (cutsLeft == 1) return BigDecimal.valueOf(cents, 2);
        long available = cents - (cutsLeft - 1L);
        long low = Math.max(1, available * 20 / 100);
        long high = Math.max(low, available * 80 / 100);
        return BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(low, high + 1), 2);
    }
}
