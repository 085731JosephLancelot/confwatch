package com.confwatch.ratelimit;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Sliding-window counter bucket for a single rate-limited key.
 * Tracks event timestamps and evicts entries outside the window.
 */
class RateLimitBucket {

    private final RateLimitPolicy policy;
    private final Deque<Long> timestamps = new ArrayDeque<>();

    RateLimitBucket(RateLimitPolicy policy) {
        this.policy = policy;
    }

    /**
     * Attempts to consume one token. Returns true if allowed.
     */
    synchronized boolean tryConsume() {
        long now = System.currentTimeMillis();
        evictExpired(now);
        if (timestamps.size() < policy.getMaxEvents()) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }

    /**
     * Returns the number of events recorded in the current window.
     */
    synchronized int currentCount() {
        evictExpired(System.currentTimeMillis());
        return timestamps.size();
    }

    private void evictExpired(long now) {
        long cutoff = now - policy.getWindowMillis();
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
            timestamps.pollFirst();
        }
    }
}
