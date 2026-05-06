package com.confwatch.pipeline;

import com.confwatch.dedup.EventDeduplicator;
import com.confwatch.filter.EventFilter;
import com.confwatch.notification.NotificationEvent;
import com.confwatch.routing.EventRouter;
import com.confwatch.throttle.ThrottleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPipelineTest {

    @Mock private EventDeduplicator deduplicator;
    @Mock private EventFilter filter;
    @Mock private ThrottleManager throttleManager;
    @Mock private EventRouter router;
    @Mock private NotificationEvent event;

    private EventPipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new EventPipeline(deduplicator, filter, throttleManager, router);
        when(event.getEventId()).thenReturn("evt-001");
        when(event.getSourcePath()).thenReturn("/etc/app/config.yaml");
    }

    @Test
    void shouldCompleteFullPipelineWhenAllStagesPass() {
        when(deduplicator.isDuplicate(event)).thenReturn(false);
        when(filter.accepts(event)).thenReturn(true);
        when(throttleManager.isThrottled(anyString())).thenReturn(false);

        PipelineResult result = pipeline.process(event);

        assertTrue(result.isCompleted());
        assertEquals(4, result.getStageResults().size());
        verify(router, times(1)).route(event);
    }

    @Test
    void shouldStopAtDeduplicationWhenDuplicate() {
        when(deduplicator.isDuplicate(event)).thenReturn(true);

        PipelineResult result = pipeline.process(event);

        assertFalse(result.isCompleted());
        assertEquals(PipelineStage.DEDUPLICATION, result.getStoppedAtStage());
        verify(filter, never()).accepts(any());
        verify(router, never()).route(any());
    }

    @Test
    void shouldStopAtFilteringWhenEventRejected() {
        when(deduplicator.isDuplicate(event)).thenReturn(false);
        when(filter.accepts(event)).thenReturn(false);

        PipelineResult result = pipeline.process(event);

        assertFalse(result.isCompleted());
        assertEquals(PipelineStage.FILTERING, result.getStoppedAtStage());
        verify(throttleManager, never()).isThrottled(anyString());
        verify(router, never()).route(any());
    }

    @Test
    void shouldStopAtThrottlingWhenThrottled() {
        when(deduplicator.isDuplicate(event)).thenReturn(false);
        when(filter.accepts(event)).thenReturn(true);
        when(throttleManager.isThrottled(anyString())).thenReturn(true);

        PipelineResult result = pipeline.process(event);

        assertFalse(result.isCompleted());
        assertEquals(PipelineStage.THROTTLING, result.getStoppedAtStage());
        verify(router, never()).route(any());
    }
}
