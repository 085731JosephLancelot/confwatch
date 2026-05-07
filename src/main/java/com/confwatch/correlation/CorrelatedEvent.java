package com.confwatch.correlation;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single file-change event eligible for correlation grouping.
 */
public class CorrelatedEvent {

    private final String filePath;
    private final String serviceName;
    private final String changeType;
    private final Instant timestamp;

    public CorrelatedEvent(String filePath, String serviceName, String changeType, Instant timestamp) {
        if (filePath == null || filePath.isBlank())
            throw new IllegalArgumentException("filePath must not be blank");
        if (changeType == null || changeType.isBlank())
            throw new IllegalArgumentException("changeType must not be blank");
        this.filePath = filePath;
        this.serviceName = serviceName;
        this.changeType = changeType;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public CorrelatedEvent(String filePath, String changeType) {
        this(filePath, null, changeType, Instant.now());
    }

    public String getFilePath() { return filePath; }
    public String getServiceName() { return serviceName; }
    public String getChangeType() { return changeType; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CorrelatedEvent)) return false;
        CorrelatedEvent that = (CorrelatedEvent) o;
        return Objects.equals(filePath, that.filePath)
                && Objects.equals(serviceName, that.serviceName)
                && Objects.equals(changeType, that.changeType)
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, serviceName, changeType, timestamp);
    }

    @Override
    public String toString() {
        return "CorrelatedEvent{filePath='" + filePath + "', service='" + serviceName
                + "', changeType='" + changeType + "', timestamp=" + timestamp + "}";
    }
}
