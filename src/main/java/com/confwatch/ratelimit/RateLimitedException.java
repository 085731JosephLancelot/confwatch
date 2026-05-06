package com.confwatch.ratelimit;

/**
 * Thrown when an operation is rejected because the rate limit
 * for a given key has been exceeded.
 */
public class RateLimitedException extends RuntimeException {

    private final String key;
    private final RateLimitPolicy policy;

    public RateLimitedException(String key, RateLimitPolicy policy) {
        super("Rate limit exceeded for key '" + key + "': " + policy);
        this.key = key;
        this.policy = policy;
    }

    public String getKey() {
        return key;
    }

    public RateLimitPolicy getPolicy() {
        return policy;
    }
}
