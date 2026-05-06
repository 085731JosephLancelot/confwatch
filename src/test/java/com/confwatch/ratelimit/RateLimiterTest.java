package com.confwatch.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    private RateLimitPolicy policy;
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        policy = new RateLimitPolicy(3, Duration.ofSeconds(5));
        rateLimiter = new RateLimiter(policy);
    }

    @Test
    void shouldAllowEventsUpToLimit() {
        assertTrue(rateLimiter.tryAcquire("service-a"));
        assertTrue(rateLimiter.tryAcquire("service-a"));
        assertTrue(rateLimiter.tryAcquire("service-a"));
    }

    @Test
    void shouldRejectEventsBeyondLimit() {
        rateLimiter.tryAcquire("service-b");
        rateLimiter.tryAcquire("service-b");
        rateLimiter.tryAcquire("service-b");
        assertFalse(rateLimiter.tryAcquire("service-b"));
    }

    @Test
    void shouldTrackCountsPerKeyIndependently() {
        rateLimiter.tryAcquire("key-x");
        rateLimiter.tryAcquire("key-x");
        rateLimiter.tryAcquire("key-y");

        assertEquals(2, rateLimiter.currentCount("key-x"));
        assertEquals(1, rateLimiter.currentCount("key-y"));
    }

    @Test
    void shouldReturnZeroCountForUnknownKey() {
        assertEquals(0, rateLimiter.currentCount("unknown"));
    }

    @Test
    void shouldResetCountForSpecificKey() {
        rateLimiter.tryAcquire("reset-key");
        rateLimiter.tryAcquire("reset-key");
        rateLimiter.reset("reset-key");
        assertEquals(0, rateLimiter.currentCount("reset-key"));
    }

    @Test
    void shouldResetAllBuckets() {
        rateLimiter.tryAcquire("a");
        rateLimiter.tryAcquire("b");
        rateLimiter.resetAll();
        assertEquals(0, rateLimiter.currentCount("a"));
        assertEquals(0, rateLimiter.currentCount("b"));
    }

    @Test
    void shouldRejectBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> rateLimiter.tryAcquire(""));
        assertThrows(IllegalArgumentException.class, () -> rateLimiter.tryAcquire(null));
    }

    @Test
    void shouldRejectNullPolicy() {
        assertThrows(IllegalArgumentException.class, () -> new RateLimiter(null));
    }

    @Test
    void rateLimitPolicyShouldRejectInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> new RateLimitPolicy(0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new RateLimitPolicy(1, Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new RateLimitPolicy(1, null));
    }
}
