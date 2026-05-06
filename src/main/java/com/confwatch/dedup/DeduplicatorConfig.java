package com.confwatch.dedup;

/**
 * Configuration for the EventDeduplicator.
 * Specifies the deduplication window and optional eviction interval.
 */
public class DeduplicatorConfig {

    public static final long DEFAULT_WINDOW_MILLIS = 2000L;
    public static final long DEFAULT_EVICTION_INTERVAL_MILLIS = 30_000L;

    private final long windowMillis;
    private final long evictionIntervalMillis;
    private final boolean enabled;

    private DeduplicatorConfig(Builder builder) {
        this.windowMillis = builder.windowMillis;
        this.evictionIntervalMillis = builder.evictionIntervalMillis;
        this.enabled = builder.enabled;
    }

    public long getWindowMillis() {
        return windowMillis;
    }

    public long getEvictionIntervalMillis() {
        return evictionIntervalMillis;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DeduplicatorConfig defaults() {
        return builder().build();
    }

    public static class Builder {
        private long windowMillis = DEFAULT_WINDOW_MILLIS;
        private long evictionIntervalMillis = DEFAULT_EVICTION_INTERVAL_MILLIS;
        private boolean enabled = true;

        public Builder windowMillis(long windowMillis) {
            this.windowMillis = windowMillis;
            return this;
        }

        public Builder evictionIntervalMillis(long evictionIntervalMillis) {
            this.evictionIntervalMillis = evictionIntervalMillis;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public DeduplicatorConfig build() {
            if (windowMillis <= 0) {
                throw new IllegalArgumentException("windowMillis must be positive");
            }
            return new DeduplicatorConfig(this);
        }
    }
}
