package com.confwatch.throttle;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ThrottlePolicyTest {

    @Test
    void constructsValidPolicy() {
        ThrottlePolicy policy = new ThrottlePolicy("svc-1", Duration.ofSeconds(5), 3, Duration.ofMinutes(1));
        assertEquals("svc-1", policy.getTargetId());
        assertEquals(Duration.ofSeconds(5), policy.getCooldown());
        assertEquals(3, policy.getMaxEventsPerWindow());
        assertEquals(Duration.ofMinutes(1), policy.getWindowDuration());
    }

    @Test
    void throwsOnBlankTargetId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThrottlePolicy(" ", Duration.ofSeconds(1), 1, Duration.ofSeconds(10)));
    }

    @Test
    void throwsOnNegativeCooldown() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThrottlePolicy("svc", Duration.ofSeconds(-1), 1, Duration.ofSeconds(10)));
    }

    @Test
    void throwsOnZeroMaxEvents() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThrottlePolicy("svc", Duration.ZERO, 0, Duration.ofSeconds(10)));
    }

    @Test
    void throwsOnZeroWindowDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThrottlePolicy("svc", Duration.ZERO, 1, Duration.ZERO));
    }

    @Test
    void equalityAndHashCode() {
        ThrottlePolicy p1 = new ThrottlePolicy("svc", Duration.ofSeconds(2), 5, Duration.ofMinutes(1));
        ThrottlePolicy p2 = new ThrottlePolicy("svc", Duration.ofSeconds(2), 5, Duration.ofMinutes(1));
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void toStringContainsTargetId() {
        ThrottlePolicy policy = new ThrottlePolicy("nginx-cfg", Duration.ofSeconds(3), 2, Duration.ofSeconds(30));
        assertTrue(policy.toString().contains("nginx-cfg"));
    }
}
