package com.confwatch.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Immutable snapshot of metrics at a point in time.
 */
public class MetricsSnapshot {

    private final Map<String, Long> counts;
    private final Instant capturedAt;
    private final Instant startedAt;

    public MetricsSnapshot(Map<String, Long> counts, Instant capturedAt, Instant startedAt) {
        this.counts = Collections.unmodifiableMap(counts);
        this.capturedAt = capturedAt;
        this.startedAt = startedAt;
    }

    public long getCount(String metric) {
        return counts.getOrDefault(metric, 0L);
    }

    public Map<String, Long> getAllCounts() {
        return counts;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Duration getUptime() {
        return Duration.between(startedAt, capturedAt);
    }

    @Override
    public String toString() {
        return "MetricsSnapshot{" +
                "capturedAt=" + capturedAt +
                ", uptime=" + getUptime().toSeconds() + "s" +
                ", counts=" + counts +
                '}';
    }
}
