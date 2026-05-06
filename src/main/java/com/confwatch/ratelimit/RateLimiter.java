package com.confwatch.ratelimit;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Token-bucket style rate limiter that restricts how many events
 * can be processed per key (e.g. file path) within a sliding window.
 */
public class RateLimiter {

    private final RateLimitPolicy policy;
    private final ConcurrentHashMap<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(RateLimitPolicy policy) {
        if (policy == null) throw new IllegalArgumentException("policy must not be null");
        this.policy = policy;
    }

    /**
     * Attempts to acquire a permit for the given key.
     *
     * @param key the resource key (e.g. file path)
     * @return true if the request is allowed, false if rate-limited
     */
    public boolean tryAcquire(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key must not be blank");
        RateLimitBucket bucket = buckets.computeIfAbsent(key, k -> new RateLimitBucket(policy));
        return bucket.tryConsume();
    }

    /**
     * Returns the current hit count within the active window for a key.
     */
    public int currentCount(String key) {
        RateLimitBucket bucket = buckets.get(key);
        return bucket == null ? 0 : bucket.currentCount();
    }

    /**
     * Resets state for a specific key (useful for testing or manual override).
     */
    public void reset(String key) {
        buckets.remove(key);
    }

    /**
     * Clears all tracked buckets.
     */
    public void resetAll() {
        buckets.clear();
    }
}
