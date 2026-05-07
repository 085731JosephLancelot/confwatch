package com.confwatch.circuit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerTest {

    private CircuitBreakerConfig config;
    private CircuitBreaker breaker;

    @BeforeEach
    void setUp() {
        config = CircuitBreakerConfig.builder()
                .failureThreshold(3)
                .openDurationMs(100L)
                .halfOpenSuccessThreshold(2)
                .build();
        breaker = new CircuitBreaker(config);
    }

    @Test
    void initialStateIsClosed() {
        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());
        assertTrue(breaker.allowRequest());
    }

    @Test
    void tripsToOpenAfterFailureThreshold() {
        breaker.recordFailure();
        breaker.recordFailure();
        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());
        breaker.recordFailure();
        assertEquals(CircuitBreaker.State.OPEN, breaker.getState());
        assertFalse(breaker.allowRequest());
    }

    @Test
    void successResetsFailureCount() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordSuccess();
        assertEquals(0, breaker.getFailureCount());
        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());
    }

    @Test
    void transitionsToHalfOpenAfterOpenDuration() throws InterruptedException {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        assertEquals(CircuitBreaker.State.OPEN, breaker.getState());
        Thread.sleep(150L);
        assertTrue(breaker.allowRequest());
        assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.getState());
    }

    @Test
    void closesAfterEnoughSuccessesInHalfOpen() throws InterruptedException {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        Thread.sleep(150L);
        breaker.allowRequest(); // triggers HALF_OPEN
        breaker.recordSuccess();
        assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.getState());
        breaker.recordSuccess();
        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());
        assertTrue(breaker.allowRequest());
    }

    @Test
    void retripsOnFailureInHalfOpen() throws InterruptedException {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        Thread.sleep(150L);
        breaker.allowRequest();
        breaker.recordFailure();
        assertEquals(CircuitBreaker.State.OPEN, breaker.getState());
    }

    @Test
    void constructorRejectsNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> new CircuitBreaker(null));
    }

    @Test
    void configBuilderValidatesFailureThreshold() {
        assertThrows(IllegalArgumentException.class, () ->
                CircuitBreakerConfig.builder().failureThreshold(0).build());
    }
}
