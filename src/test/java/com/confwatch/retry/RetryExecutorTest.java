package com.confwatch.retry;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryExecutorTest {

    @Test
    void succeedsOnFirstAttempt() throws RetryExhaustedException {
        RetryExecutor executor = new RetryExecutor(RetryPolicy.withDefaults());
        String result = executor.execute(() -> "ok", "test-task");
        assertEquals("ok", result);
    }

    @Test
    void retriesAndSucceeds() throws RetryExhaustedException {
        AtomicInteger counter = new AtomicInteger(0);
        RetryPolicy policy = new RetryPolicy(3, 0, 1.0, 0);
        RetryExecutor executor = new RetryExecutor(policy);

        String result = executor.execute(() -> {
            if (counter.incrementAndGet() < 3) throw new RuntimeException("not yet");
            return "done";
        }, "flaky-task");

        assertEquals("done", result);
        assertEquals(3, counter.get());
    }

    @Test
    void throwsRetryExhaustedAfterAllAttempts() {
        RetryPolicy policy = new RetryPolicy(2, 0, 1.0, 0);
        RetryExecutor executor = new RetryExecutor(policy);

        RetryExhaustedException ex = assertThrows(RetryExhaustedException.class, () ->
            executor.execute(() -> { throw new RuntimeException("always fails"); }, "bad-task")
        );

        assertEquals("bad-task", ex.getTaskName());
        assertEquals(2, ex.getAttemptsUsed());
    }

    @Test
    void noRetryPolicyFailsImmediately() {
        RetryExecutor executor = new RetryExecutor(RetryPolicy.noRetry());
        AtomicInteger counter = new AtomicInteger(0);

        assertThrows(RetryExhaustedException.class, () ->
            executor.execute(() -> { counter.incrementAndGet(); throw new RuntimeException("fail"); }, "single")
        );
        assertEquals(1, counter.get());
    }

    @Test
    void delayForAttemptCalculation() {
        RetryPolicy policy = new RetryPolicy(5, 100, 2.0, 5000);
        assertEquals(0, policy.delayForAttempt(1));
        assertEquals(100, policy.delayForAttempt(2));
        assertEquals(200, policy.delayForAttempt(3));
        assertEquals(400, policy.delayForAttempt(4));
        assertEquals(800, policy.delayForAttempt(5));
    }

    @Test
    void delayIsCappedAtMaxDelay() {
        RetryPolicy policy = new RetryPolicy(10, 1000, 2.0, 3000);
        assertEquals(3000, policy.delayForAttempt(6));
        assertEquals(3000, policy.delayForAttempt(10));
    }

    @Test
    void constructorValidatesArguments() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(0, 100, 2.0, 1000));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(3, -1, 2.0, 1000));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(3, 100, 0.5, 1000));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(3, 500, 2.0, 100));
    }
}
