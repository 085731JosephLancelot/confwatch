package com.confwatch.replay;

import com.confwatch.audit.AuditLogger;
import com.confwatch.notification.NotificationEvent;
import com.confwatch.pipeline.EventPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventReplayManagerTest {

    @Mock private EventPipeline pipeline;
    @Mock private AuditLogger auditLogger;
    @Mock private ReplayableEvent event1;
    @Mock private ReplayableEvent event2;
    @Mock private NotificationEvent notificationEvent;

    private ReplayConfig config;
    private EventReplayManager manager;

    @BeforeEach
    void setUp() {
        config = ReplayConfig.builder()
                .maxStoredEvents(100)
                .retentionDuration(Duration.ofHours(1))
                .build();
        manager = new EventReplayManager(pipeline, auditLogger, config);
    }

    @Test
    void constructor_rejectsNullArguments() {
        assertThatThrownBy(() -> new EventReplayManager(null, auditLogger, config))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EventReplayManager(pipeline, null, config))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EventReplayManager(pipeline, auditLogger, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void record_incrementsStoredCount() {
        Instant now = Instant.now();
        when(event1.getOccurredAt()).thenReturn(now);
        manager.record(event1);
        assertThat(manager.getStoredEventCount()).isEqualTo(1);
    }

    @Test
    void record_nullEventIsIgnored() {
        manager.record(null);
        assertThat(manager.getStoredEventCount()).isZero();
    }

    @Test
    void getEventsInWindow_returnsMatchingEvents() {
        Instant base = Instant.now();
        when(event1.getOccurredAt()).thenReturn(base.minusSeconds(30));
        when(event2.getOccurredAt()).thenReturn(base.minusSeconds(120));
        manager.record(event1);
        manager.record(event2);

        var result = manager.getEventsInWindow(base.minusSeconds(60), base);
        assertThat(result).containsExactly(event1);
    }

    @Test
    void getEventsInWindow_rejectsInvalidRange() {
        Instant now = Instant.now();
        assertThatThrownBy(() -> manager.getEventsInWindow(now, now.minusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replay_submitsMatchingEventsToPipeline() throws Exception {
        Instant base = Instant.now();
        when(event1.getOccurredAt()).thenReturn(base.minusSeconds(10));
        when(event1.toNotificationEvent()).thenReturn(notificationEvent);
        when(event1.getFilePath()).thenReturn("/etc/app/config.yaml");
        manager.record(event1);

        ReplayResult result = manager.replay(base.minusSeconds(60), base);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getSucceeded()).isEqualTo(1);
        assertThat(result.getFailed()).isZero();
        verify(pipeline).submit(notificationEvent);
    }

    @Test
    void replay_countsFailuresWhenPipelineThrows() throws Exception {
        Instant base = Instant.now();
        when(event1.getOccurredAt()).thenReturn(base.minusSeconds(5));
        when(event1.toNotificationEvent()).thenReturn(notificationEvent);
        when(event1.getFilePath()).thenReturn("/etc/app/config.yaml");
        doThrow(new RuntimeException("pipeline error")).when(pipeline).submit(any());
        manager.record(event1);

        ReplayResult result = manager.replay(base.minusSeconds(60), base);

        assertThat(result.getFailed()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    void replayConfig_defaultsAreValid() {
        ReplayConfig defaults = ReplayConfig.defaults();
        assertThat(defaults.getMaxStoredEvents()).isGreaterThan(0);
        assertThat(defaults.getRetentionDuration()).isPositive();
    }
}
