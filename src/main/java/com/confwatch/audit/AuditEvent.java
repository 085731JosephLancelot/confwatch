package com.confwatch.audit;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single audit event capturing a config file change and the action taken.
 */
public class AuditEvent {

    public enum EventType {
        FILE_CHANGED,
        ACTION_TRIGGERED,
        ACTION_FAILED,
        RELOAD_TRIGGERED
    }

    private final Instant timestamp;
    private final EventType eventType;
    private final String filePath;
    private final String details;
    private final boolean success;

    public AuditEvent(EventType eventType, String filePath, String details, boolean success) {
        this.timestamp = Instant.now();
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
        this.details = details;
        this.success = success;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDetails() {
        return details;
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns true if this event represents a failure, i.e., the event type is
     * {@link EventType#ACTION_FAILED} or the action was attempted but did not succeed.
     *
     * @return true if this is a failure event
     */
    public boolean isFailure() {
        return eventType == EventType.ACTION_FAILED || !success;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | file=%s | success=%b | details=%s",
                timestamp, eventType, filePath, success, details);
    }
}
