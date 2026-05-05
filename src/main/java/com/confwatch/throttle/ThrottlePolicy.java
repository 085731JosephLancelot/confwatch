package com.confwatch.throttle;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines a throttle policy for limiting how frequently actions are triggered
 * for a given watch target.
 */
public class ThrottlePolicy {

    private final String targetId;
    private final Duration cooldown;
    private final int maxEventsPerWindow;
    private final Duration windowDuration;

    public ThrottlePolicy(String targetId, Duration cooldown, int maxEventsPerWindow, Duration windowDuration) {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank");
        }
        if (cooldown == null || cooldown.isNegative()) {
            throw new IllegalArgumentException("cooldown must be a non-negative duration");
        }
        if (maxEventsPerWindow < 1) {
            throw new IllegalArgumentException("maxEventsPerWindow must be at least 1");
        }
        if (windowDuration == null || windowDuration.isNegative() || windowDuration.isZero()) {
            throw new IllegalArgumentException("windowDuration must be a positive duration");
        }
        this.targetId = targetId;
        this.cooldown = cooldown;
        this.maxEventsPerWindow = maxEventsPerWindow;
        this.windowDuration = windowDuration;
    }

    public String getTargetId() {
        return targetId;
    }

    public Duration getCooldown() {
        return cooldown;
    }

    public int getMaxEventsPerWindow() {
        return maxEventsPerWindow;
    }

    public Duration getWindowDuration() {
        return windowDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThrottlePolicy)) return false;
        ThrottlePolicy that = (ThrottlePolicy) o;
        return maxEventsPerWindow == that.maxEventsPerWindow
                && Objects.equals(targetId, that.targetId)
                && Objects.equals(cooldown, that.cooldown)
                && Objects.equals(windowDuration, that.windowDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetId, cooldown, maxEventsPerWindow, windowDuration);
    }

    @Override
    public String toString() {
        return "ThrottlePolicy{targetId='" + targetId + "', cooldown=" + cooldown
                + ", maxEventsPerWindow=" + maxEventsPerWindow
                + ", windowDuration=" + windowDuration + '}';
    }
}
