package com.confwatch.circuit;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker to prevent cascading failures when actions or webhooks
 * repeatedly fail. Transitions between CLOSED, OPEN, and HALF_OPEN states.
 */
public class CircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final CircuitBreakerConfig config;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private volatile Instant openedAt = null;

    public CircuitBreaker(CircuitBreakerConfig config) {
        if (config == null) throw new IllegalArgumentException("CircuitBreakerConfig must not be null");
        this.config = config;
    }

    /**
     * Returns true if the circuit allows the call to proceed.
     */
    public boolean allowRequest() {
        State current = state.get();
        if (current == State.CLOSED) {
            return true;
        }
        if (current == State.OPEN) {
            if (openedAt != null &&
                    Instant.now().isAfter(openedAt.plusMillis(config.getOpenDurationMs()))) {
                if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                    successCount.set(0);
                }
                return true;
            }
            return false;
        }
        // HALF_OPEN: allow limited probe
        return true;
    }

    public void recordSuccess() {
        if (state.get() == State.HALF_OPEN) {
            if (successCount.incrementAndGet() >= config.getHalfOpenSuccessThreshold()) {
                reset();
            }
        } else {
            failureCount.set(0);
        }
    }

    public void recordFailure() {
        int failures = failureCount.incrementAndGet();
        if (state.get() == State.HALF_OPEN) {
            trip();
        } else if (failures >= config.getFailureThreshold()) {
            trip();
        }
    }

    private void trip() {
        state.set(State.OPEN);
        openedAt = Instant.now();
        failureCount.set(0);
    }

    private void reset() {
        state.set(State.CLOSED);
        failureCount.set(0);
        successCount.set(0);
        openedAt = null;
    }

    public State getState() { return state.get(); }
    public int getFailureCount() { return failureCount.get(); }
}
