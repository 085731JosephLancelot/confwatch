package com.confwatch.ratelimit;

import java.time.Duration;

/**
 * Immutable configuration for a rate-limiting policy.
 * Defines the maximum number of events allowed within a time window.
 */
public class RateLimitPolicy {

    private final int maxEvents;
    private final Duration windowDuration;

    public RateLimitPolicy(int maxEvents, Duration windowDuration) {
        if (maxEvents <= 0) throw new IllegalArgumentException("maxEvents must be positive");
        if (windowDuration == null || windowDuration.isNegative() || windowDuration.isZero()) {
            throw new IllegalArgumentException("windowDuration must be positive");
        }
        this.maxEvents = maxEvents;
        this.windowDuration = windowDuration;
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public Duration getWindowDuration() {
        return windowDuration;
    }

    public long getWindowMillis() {
        return windowDuration.toMillis();
    }

    @Override
    public String toString() {
        return "RateLimitPolicy{maxEvents=" + maxEvents +
                ", windowDuration=" + windowDuration + "}";
    }
}
