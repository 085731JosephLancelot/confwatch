package com.confwatch.health;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthStatusTest {

    @Test
    void shouldReportHealthyState() {
        Map<String, String> components = new HashMap<>();
        components.put("fileWatcher", "OK");
        components.put("actionExecutor", "OK");

        HealthStatus status = new HealthStatus(HealthStatus.State.HEALTHY, 3600L, components);

        assertEquals(HealthStatus.State.HEALTHY, status.getState());
        assertTrue(status.isHealthy());
        assertEquals(3600L, status.getUptimeSeconds());
    }

    @Test
    void shouldReportDegradedState() {
        Map<String, String> components = new HashMap<>();
        components.put("fileWatcher", "OK");
        components.put("actionExecutor", "WARN: slow response");

        HealthStatus status = new HealthStatus(HealthStatus.State.DEGRADED, 120L, components);

        assertEquals(HealthStatus.State.DEGRADED, status.getState());
        assertFalse(status.isHealthy());
    }

    @Test
    void shouldReportUnhealthyState() {
        Map<String, String> components = new HashMap<>();
        components.put("fileWatcher", "ERROR: thread dead");
        components.put("actionExecutor", "ERROR: queue full");

        HealthStatus status = new HealthStatus(HealthStatus.State.UNHEALTHY, 10L, components);

        assertEquals(HealthStatus.State.UNHEALTHY, status.getState());
        assertFalse(status.isHealthy());
        assertEquals(2, status.getComponents().size());
    }

    @Test
    void shouldRecordCheckedAtTimestamp() {
        Instant before = Instant.now();
        HealthStatus status = new HealthStatus(HealthStatus.State.HEALTHY, 0L, new HashMap<>());
        Instant after = Instant.now();

        assertNotNull(status.getCheckedAt());
        assertFalse(status.getCheckedAt().isBefore(before));
        assertFalse(status.getCheckedAt().isAfter(after));
    }

    @Test
    void componentsShouldBeImmutable() {
        Map<String, String> components = new HashMap<>();
        components.put("fileWatcher", "OK");

        HealthStatus status = new HealthStatus(HealthStatus.State.HEALTHY, 60L, components);

        assertThrows(UnsupportedOperationException.class,
            () -> status.getComponents().put("newComponent", "OK"));
    }

    @Test
    void toStringShouldContainStateAndUptime() {
        HealthStatus status = new HealthStatus(HealthStatus.State.HEALTHY, 500L, new HashMap<>());
        String str = status.toString();

        assertTrue(str.contains("HEALTHY"));
        assertTrue(str.contains("500"));
    }
}
