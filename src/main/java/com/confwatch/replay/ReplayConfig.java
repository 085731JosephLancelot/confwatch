package com.confwatch.replay;

import java.time.Duration;

/**
 * Configuration for EventReplayManager controlling retention and capacity.
 */
public class ReplayConfig {

    private static final int DEFAULT_MAX_STORED_EVENTS = 1000;
    private static final Duration DEFAULT_RETENTION = Duration.ofHours(24);

    private final int maxStoredEvents;
    private final Duration retentionDuration;

    private ReplayConfig(Builder builder) {
        this.maxStoredEvents = builder.maxStoredEvents;
        this.retentionDuration = builder.retentionDuration;
    }

    public int getMaxStoredEvents() {
        return maxStoredEvents;
    }

    public Duration getRetentionDuration() {
        return retentionDuration;
    }

    public static ReplayConfig defaults() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxStoredEvents = DEFAULT_MAX_STORED_EVENTS;
        private Duration retentionDuration = DEFAULT_RETENTION;

        public Builder maxStoredEvents(int maxStoredEvents) {
            if (maxStoredEvents <= 0) throw new IllegalArgumentException("maxStoredEvents must be positive");
            this.maxStoredEvents = maxStoredEvents;
            return this;
        }

        public Builder retentionDuration(Duration retentionDuration) {
            if (retentionDuration == null || retentionDuration.isNegative() || retentionDuration.isZero()) {
                throw new IllegalArgumentException("retentionDuration must be positive");
            }
            this.retentionDuration = retentionDuration;
            return this;
        }

        public ReplayConfig build() {
            return new ReplayConfig(this);
        }
    }
}
