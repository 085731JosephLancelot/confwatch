package com.confwatch.correlation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventCorrelatorTest {

    private EventCorrelator correlator;

    @BeforeEach
    void setUp() {
        CorrelationConfig config = CorrelationConfig.builder()
                .windowMillis(500)
                .maxGroupSize(3)
                .groupByService(false)
                .build();
        correlator = new EventCorrelator(config);
    }

    @Test
    void submitBelowMaxSizeReturnsEmpty() {
        CorrelatedEvent event = new CorrelatedEvent("/etc/app/config.yml", "MODIFY");
        List<CorrelatedEvent> result = correlator.submit(event);
        assertTrue(result.isEmpty());
        assertEquals(1, correlator.pendingGroupCount());
    }

    @Test
    void submitAtMaxSizeFlushesGroup() {
        String path = "/etc/app/config.yml";
        correlator.submit(new CorrelatedEvent(path, "MODIFY"));
        correlator.submit(new CorrelatedEvent(path, "MODIFY"));
        List<CorrelatedEvent> flushed = correlator.submit(new CorrelatedEvent(path, "MODIFY"));

        assertEquals(3, flushed.size());
        assertEquals(0, correlator.pendingGroupCount());
    }

    @Test
    void differentPathsGroupSeparately() {
        correlator.submit(new CorrelatedEvent("/etc/a.conf", "MODIFY"));
        correlator.submit(new CorrelatedEvent("/etc/b.conf", "MODIFY"));
        assertEquals(2, correlator.pendingGroupCount());
    }

    @Test
    void groupByServiceGroupsUnderServiceKey() {
        CorrelationConfig config = CorrelationConfig.builder()
                .windowMillis(500)
                .maxGroupSize(3)
                .groupByService(true)
                .build();
        EventCorrelator svcCorrelator = new EventCorrelator(config);

        svcCorrelator.submit(new CorrelatedEvent("/etc/a.conf", "svc-auth", "MODIFY", Instant.now()));
        svcCorrelator.submit(new CorrelatedEvent("/etc/b.conf", "svc-auth", "MODIFY", Instant.now()));
        assertEquals(1, svcCorrelator.pendingGroupCount());
    }

    @Test
    void flushExpiredReturnsStaleGroups() throws InterruptedException {
        CorrelationConfig config = CorrelationConfig.builder()
                .windowMillis(50)
                .maxGroupSize(10)
                .build();
        EventCorrelator shortCorrelator = new EventCorrelator(config);

        shortCorrelator.submit(new CorrelatedEvent("/etc/app.conf", "MODIFY"));
        Thread.sleep(100);

        Map<String, List<CorrelatedEvent>> expired = shortCorrelator.flushExpired();
        assertEquals(1, expired.size());
        assertEquals(0, shortCorrelator.pendingGroupCount());
    }

    @Test
    void submitNullEventThrows() {
        assertThrows(IllegalArgumentException.class, () -> correlator.submit(null));
    }

    @Test
    void constructorNullConfigThrows() {
        assertThrows(IllegalArgumentException.class, () -> new EventCorrelator(null));
    }
}
