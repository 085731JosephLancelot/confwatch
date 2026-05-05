package com.confwatch.health;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the health status of the confwatch daemon,
 * including watcher state, action executor status, and uptime.
 */
public class HealthStatus {

    public enum State {
        HEALTHY, DEGRADED, UNHEALTHY
    }

    private final State state;
    private final Instant checkedAt;
    private final Map<String, String> components;
    private final long uptimeSeconds;

    public HealthStatus(State state, long uptimeSeconds, Map<String, String> components) {
        this.state = state;
        this.checkedAt = Instant.now();
        this.uptimeSeconds = uptimeSeconds;
        this.components = Collections.unmodifiableMap(new HashMap<>(components));
    }

    public State getState() {
        return state;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public Map<String, String> getComponents() {
        return components;
    }

    public boolean isHealthy() {
        return state == State.HEALTHY;
    }

    @Override
    public String toString() {
        return "HealthStatus{state=" + state +
               ", uptimeSeconds=" + uptimeSeconds +
               ", checkedAt=" + checkedAt +
               ", components=" + components + "}";
    }
}
