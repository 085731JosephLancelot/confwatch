package com.confwatch.notification;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a file change event to be dispatched to notification channels.
 */
public class NotificationEvent {

    private final String filePath;
    private final String changeType;
    private final Instant timestamp;
    private final String serviceLabel;

    public NotificationEvent(String filePath, String changeType, String serviceLabel) {
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
        this.changeType = Objects.requireNonNull(changeType, "changeType must not be null");
        this.serviceLabel = serviceLabel != null ? serviceLabel : "unknown";
        this.timestamp = Instant.now();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getChangeType() {
        return changeType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getServiceLabel() {
        return serviceLabel;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s changed (%s) in service '%s'",
                timestamp, filePath, changeType, serviceLabel);
    }
}
