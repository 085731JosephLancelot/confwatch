package com.confwatch.retry;

/**
 * Defines the retry policy for failed actions (webhooks, reload commands).
 */
public class RetryPolicy {

    private final int maxAttempts;
    private final long initialDelayMs;
    private final double backoffMultiplier;
    private final long maxDelayMs;

    public RetryPolicy(int maxAttempts, long initialDelayMs, double backoffMultiplier, long maxDelayMs) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (initialDelayMs < 0) throw new IllegalArgumentException("initialDelayMs must be >= 0");
        if (backoffMultiplier < 1.0) throw new IllegalArgumentException("backoffMultiplier must be >= 1.0");
        if (maxDelayMs < initialDelayMs) throw new IllegalArgumentException("maxDelayMs must be >= initialDelayMs");
        this.maxAttempts = maxAttempts;
        this.initialDelayMs = initialDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.maxDelayMs = maxDelayMs;
    }

    public static RetryPolicy noRetry() {
        return new RetryPolicy(1, 0, 1.0, 0);
    }

    public static RetryPolicy withDefaults() {
        return new RetryPolicy(3, 500, 2.0, 10_000);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public long getMaxDelayMs() {
        return maxDelayMs;
    }

    /**
     * Calculates the delay before the given attempt number (1-based).
     */
    public long delayForAttempt(int attemptNumber) {
        if (attemptNumber <= 1) return 0;
        long delay = (long) (initialDelayMs * Math.pow(backoffMultiplier, attemptNumber - 2));
        return Math.min(delay, maxDelayMs);
    }

    /**
     * Returns whether a retry should be attempted after the given attempt number (1-based).
     * Returns {@code true} if the attempt number is less than {@code maxAttempts},
     * meaning there are still retries remaining.
     *
     * @param attemptNumber the 1-based number of the attempt that just completed
     * @return {@code true} if another attempt should be made, {@code false} otherwise
     */
    public boolean shouldRetry(int attemptNumber) {
        return attemptNumber < maxAttempts;
    }

    @Override
    public String toString() {
        return "RetryPolicy{maxAttempts=" + maxAttempts +
               ", initialDelayMs=" + initialDelayMs +
               ", backoffMultiplier=" + backoffMultiplier +
               ", maxDelayMs=" + maxDelayMs + "}";
    }
}
