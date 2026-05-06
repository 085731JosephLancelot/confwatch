package com.confwatch.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCollectorTest {

    private MetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new MetricsCollector();
    }

    @Test
    void initialCountsAreZero() {
        assertEquals(0L, collector.getCount(MetricsCollector.FILE_CHANGES_DETECTED));
        assertEquals(0L, collector.getCount(MetricsCollector.WEBHOOKS_DISPATCHED));
        assertEquals(0L, collector.getCount(MetricsCollector.ACTIONS_FAILED));
    }

    @Test
    void incrementIncreasesCountByOne() {
        collector.increment(MetricsCollector.FILE_CHANGES_DETECTED);
        collector.increment(MetricsCollector.FILE_CHANGES_DETECTED);
        assertEquals(2L, collector.getCount(MetricsCollector.FILE_CHANGES_DETECTED));
    }

    @Test
    void incrementByAddsCorrectAmount() {
        collector.incrementBy(MetricsCollector.RETRIES_ATTEMPTED, 5);
        assertEquals(5L, collector.getCount(MetricsCollector.RETRIES_ATTEMPTED));
    }

    @Test
    void incrementByIgnoresNonPositiveValues() {
        collector.incrementBy(MetricsCollector.RETRIES_ATTEMPTED, 0);
        collector.incrementBy(MetricsCollector.RETRIES_ATTEMPTED, -3);
        assertEquals(0L, collector.getCount(MetricsCollector.RETRIES_ATTEMPTED));
    }

    @Test
    void getLastEventTimeUpdatedOnIncrement() {
        Instant before = Instant.now();
        collector.increment(MetricsCollector.WEBHOOKS_DISPATCHED);
        Instant last = collector.getLastEventTime(MetricsCollector.WEBHOOKS_DISPATCHED);
        assertNotNull(last);
        assertFalse(last.isBefore(before));
    }

    @Test
    void snapshotCapturesCurrentCounts() {
        collector.increment(MetricsCollector.ACTIONS_EXECUTED);
        collector.increment(MetricsCollector.ACTIONS_EXECUTED);
        collector.increment(MetricsCollector.THROTTLE_SKIPS);

        MetricsSnapshot snapshot = collector.snapshot();

        assertEquals(2L, snapshot.getCount(MetricsCollector.ACTIONS_EXECUTED));
        assertEquals(1L, snapshot.getCount(MetricsCollector.THROTTLE_SKIPS));
        assertNotNull(snapshot.getCapturedAt());
        assertNotNull(snapshot.getStartedAt());
        assertFalse(snapshot.getUptime().isNegative());
    }

    @Test
    void resetClearsAllCounters() {
        collector.increment(MetricsCollector.WEBHOOKS_FAILED);
        collector.increment(MetricsCollector.ACTIONS_FAILED);
        collector.reset();

        assertEquals(0L, collector.getCount(MetricsCollector.WEBHOOKS_FAILED));
        assertEquals(0L, collector.getCount(MetricsCollector.ACTIONS_FAILED));
        assertNotNull(collector.getStartTime());
    }

    @Test
    void unknownMetricReturnsZero() {
        assertEquals(0L, collector.getCount("nonexistent.metric"));
    }
}
