package com.confwatch.replay;

import com.confwatch.audit.AuditEvent;
import com.confwatch.audit.AuditLogger;
import com.confwatch.pipeline.EventPipeline;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Manages replay of historical events within a configurable time window.
 * Allows re-processing of file change events for recovery or debugging.
 */
public class EventReplayManager {

    private final EventPipeline pipeline;
    private final AuditLogger auditLogger;
    private final ReplayConfig config;
    private final CopyOnWriteArrayList<ReplayableEvent> eventStore;

    public EventReplayManager(EventPipeline pipeline, AuditLogger auditLogger, ReplayConfig config) {
        if (pipeline == null) throw new IllegalArgumentException("pipeline must not be null");
        if (auditLogger == null) throw new IllegalArgumentException("auditLogger must not be null");
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.pipeline = pipeline;
        this.auditLogger = auditLogger;
        this.config = config;
        this.eventStore = new CopyOnWriteArrayList<>();
    }

    public void record(ReplayableEvent event) {
        if (event == null) return;
        evictExpired();
        if (eventStore.size() >= config.getMaxStoredEvents()) {
            eventStore.remove(0);
        }
        eventStore.add(event);
    }

    public List<ReplayableEvent> getEventsInWindow(Instant from, Instant to) {
        if (from == null || to == null) throw new IllegalArgumentException("from and to must not be null");
        if (from.isAfter(to)) throw new IllegalArgumentException("from must not be after to");
        return eventStore.stream()
                .filter(e -> !e.getOccurredAt().isBefore(from) && !e.getOccurredAt().isAfter(to))
                .collect(Collectors.toList());
    }

    public ReplayResult replay(Instant from, Instant to) {
        List<ReplayableEvent> candidates = getEventsInWindow(from, to);
        int succeeded = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (ReplayableEvent event : candidates) {
            try {
                pipeline.submit(event.toNotificationEvent());
                auditLogger.log(AuditEvent.of("REPLAY", event.getFilePath(), "replayed successfully"));
                succeeded++;
            } catch (Exception ex) {
                failed++;
                errors.add(event.getFilePath() + ": " + ex.getMessage());
            }
        }
        return new ReplayResult(candidates.size(), succeeded, failed, errors);
    }

    public int getStoredEventCount() {
        return eventStore.size();
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(config.getRetentionDuration());
        eventStore.removeIf(e -> e.getOccurredAt().isBefore(cutoff));
    }
}
