package com.confwatch.metrics;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects and tracks runtime metrics for confwatch operations.
 * Tracks counts of file change events, webhook dispatches, retries, and failures.
 */
public class MetricsCollector {

    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, Instant> timestamps = new ConcurrentHashMap<>();

    public static final String FILE_CHANGES_DETECTED = "file.changes.detected";
    public static final String WEBHOOKS_DISPATCHED    = "webhooks.dispatched";
    public static final String WEBHOOKS_FAILED        = "webhooks.failed";
    public static final String RETRIES_ATTEMPTED      = "retries.attempted";
    public static final String ACTIONS_EXECUTED       = "actions.executed";
    public static final String ACTIONS_FAILED         = "actions.failed";
    public static final String THROTTLE_SKIPS         = "throttle.skips";

    public MetricsCollector() {
        for (String key : new String[]{
                FILE_CHANGES_DETECTED, WEBHOOKS_DISPATCHED, WEBHOOKS_FAILED,
                RETRIES_ATTEMPTED, ACTIONS_EXECUTED, ACTIONS_FAILED, THROTTLE_SKIPS}) {
            counters.put(key, new AtomicLong(0));
        }
        timestamps.put("started.at", Instant.now());
    }

    public void increment(String metric) {
        counters.computeIfAbsent(metric, k -> new AtomicLong(0)).incrementAndGet();
        timestamps.put(metric + ".last", Instant.now());
    }

    public void incrementBy(String metric, long amount) {
        if (amount <= 0) return;
        counters.computeIfAbsent(metric, k -> new AtomicLong(0)).addAndGet(amount);
        timestamps.put(metric + ".last", Instant.now());
    }

    public long getCount(String metric) {
        AtomicLong counter = counters.get(metric);
        return counter == null ? 0L : counter.get();
    }

    public Instant getLastEventTime(String metric) {
        return timestamps.get(metric + ".last");
    }

    public Instant getStartTime() {
        return timestamps.get("started.at");
    }

    public MetricsSnapshot snapshot() {
        Map<String, Long> counts = new ConcurrentHashMap<>();
        counters.forEach((k, v) -> counts.put(k, v.get()));
        return new MetricsSnapshot(counts, Instant.now(), getStartTime());
    }

    public void reset() {
        counters.values().forEach(c -> c.set(0));
        Instant now = Instant.now();
        timestamps.replaceAll((k, v) -> k.equals("started.at") ? now : null);
        timestamps.put("started.at", now);
    }
}
