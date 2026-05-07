package com.confwatch.circuit;

/**
 * Configuration for a {@link CircuitBreaker} instance.
 */
public class CircuitBreakerConfig {

    private final int failureThreshold;
    private final long openDurationMs;
    private final int halfOpenSuccessThreshold;

    private CircuitBreakerConfig(Builder builder) {
        this.failureThreshold = builder.failureThreshold;
        this.openDurationMs = builder.openDurationMs;
        this.halfOpenSuccessThreshold = builder.halfOpenSuccessThreshold;
    }

    public int getFailureThreshold() { return failureThreshold; }
    public long getOpenDurationMs() { return openDurationMs; }
    public int getHalfOpenSuccessThreshold() { return halfOpenSuccessThreshold; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int failureThreshold = 5;
        private long openDurationMs = 30_000L;
        private int halfOpenSuccessThreshold = 2;

        public Builder failureThreshold(int failureThreshold) {
            if (failureThreshold < 1) throw new IllegalArgumentException("failureThreshold must be >= 1");
            this.failureThreshold = failureThreshold;
            return this;
        }

        public Builder openDurationMs(long openDurationMs) {
            if (openDurationMs < 0) throw new IllegalArgumentException("openDurationMs must be >= 0");
            this.openDurationMs = openDurationMs;
            return this;
        }

        public Builder halfOpenSuccessThreshold(int halfOpenSuccessThreshold) {
            if (halfOpenSuccessThreshold < 1)
                throw new IllegalArgumentException("halfOpenSuccessThreshold must be >= 1");
            this.halfOpenSuccessThreshold = halfOpenSuccessThreshold;
            return this;
        }

        public CircuitBreakerConfig build() { return new CircuitBreakerConfig(this); }
    }
}
