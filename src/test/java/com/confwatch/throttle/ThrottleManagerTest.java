package com.confwatch.throttle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ThrottleManagerTest {

    private ThrottleManager manager;
    private static final String TARGET = "app-config";
    private static final Instant T0 = Instant.parse("2024-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        manager = new ThrottleManager();
    }

    @Test
    void allowsActionWhenNoPolicyRegistered() {
        assertTrue(manager.tryAcquire("unknown-target", T0));
    }

    @Test
    void allowsFirstActionAfterRegistration() {
        manager.registerPolicy(new ThrottlePolicy(TARGET, Duration.ofSeconds(5), 3, Duration.ofMinutes(1)));
        assertTrue(manager.tryAcquire(TARGET, T0));
    }

    @Test
    void suppressesActionWithinCooldown() {
        manager.registerPolicy(new ThrottlePolicy(TARGET, Duration.ofSeconds(10), 10, Duration.ofMinutes(1)));
        assertTrue(manager.tryAcquire(TARGET, T0));
        assertFalse(manager.tryAcquire(TARGET, T0.plusSeconds(5)));
    }

    @Test
    void allowsActionAfterCooldownExpires() {
        manager.registerPolicy(new ThrottlePolicy(TARGET, Duration.ofSeconds(10), 10, Duration.ofMinutes(1)));
        assertTrue(manager.tryAcquire(TARGET, T0));
        assertTrue(manager.tryAcquire(TARGET, T0.plusSeconds(11)));
    }

    @Test
    void suppressesActionWhenRateWindowExceeded() {
        manager.registerPolicy(new ThrottlePolicy(TARGET, Duration.ZERO, 2, Duration.ofMinutes(1)));
        assertTrue(manager.tryAcquire(TARGET, T0));
        assertTrue(manager.tryAcquire(TARGET, T0.plusSeconds(1)));
        assertFalse(manager.tryAcquire(TARGET, T0.plusSeconds(2)));
    }

    @Test
    void allowsActionAfterWindowSlides() {
        manager.registerPolicy(new ThrottlePolicy(TARGET, Duration.ZERO, 2, Duration.ofSeconds(30)));
        assertTrue(manager.tryAcquire(TARGET, T0));
        assertTrue(manager.tryAcquire(TARGET, T0.plusSeconds(1)));
        // Both old events are now outside the 30-second window
        assertTrue(manager.tryAcquire(TARGET, T0.plusSeconds(60)));
    }

    @Test
    void removingPolicyAllowsUnthrottledActions() {
        manager.registerPolicy(new ThrottlePolicy(TARGET, Duration.ofSeconds(60), 1, Duration.ofMinutes(5)));
        assertTrue(manager.tryAcquire(TARGET, T0));
        assertFalse(manager.tryAcquire(TARGET, T0.plusSeconds(1)));
        manager.removePolicy(TARGET);
        assertFalse(manager.hasPolicy(TARGET));
        assertTrue(manager.tryAcquire(TARGET, T0.plusSeconds(2)));
    }

    @Test
    void registerPolicyReturnsTrueForHasPolicy() {
        assertFalse(manager.hasPolicy(TARGET));
        manager.registerPolicy(new ThrottlePolicy(TARGET, Duration.ZERO, 5, Duration.ofMinutes(1)));
        assertTrue(manager.hasPolicy(TARGET));
    }
}
