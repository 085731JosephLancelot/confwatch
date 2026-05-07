package com.confwatch.correlation;

/**
 * Configuration for event correlation behaviour.
 */
public class CorrelationConfig {

    private static final long DEFAULT_WINDOW_MILLIS = 2000L;
    private static final int DEFAULT_MAX_GROUP_SIZE = 20;

    private final long windowMillis;
    private final int maxGroupSize;
    private final boolean groupByService;

    private CorrelationConfig(Builder builder) {
        this.windowMillis = builder.windowMillis;
        this.maxGroupSize = builder.maxGroupSize;
        this.groupByService = builder.groupByService;
    }

    public long getWindowMillis() { return windowMillis; }
    public int getMaxGroupSize() { return maxGroupSize; }
    public boolean isGroupByService() { return groupByService; }

    public static Builder builder() { return new Builder(); }

    public static CorrelationConfig defaults() {
        return builder().build();
    }

    public static class Builder {
        private long windowMillis = DEFAULT_WINDOW_MILLIS;
        private int maxGroupSize = DEFAULT_MAX_GROUP_SIZE;
        private boolean groupByService = false;

        public Builder windowMillis(long windowMillis) {
            if (windowMillis <= 0) throw new IllegalArgumentException("windowMillis must be positive");
            this.windowMillis = windowMillis;
            return this;
        }

        public Builder maxGroupSize(int maxGroupSize) {
            if (maxGroupSize < 1) throw new IllegalArgumentException("maxGroupSize must be >= 1");
            this.maxGroupSize = maxGroupSize;
            return this;
        }

        public Builder groupByService(boolean groupByService) {
            this.groupByService = groupByService;
            return this;
        }

        public CorrelationConfig build() {
            return new CorrelationConfig(this);
        }
    }
}
