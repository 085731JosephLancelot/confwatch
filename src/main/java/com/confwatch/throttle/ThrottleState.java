package com.confwatch.throttle;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks the runtime throttle state for a single watch target.
 */
class ThrottleState {

    private Instant lastActionTime;
    private final Deque<Instant> eventTimestamps = new ArrayDeque<>();

    ThrottleState() {
        this.lastActionTime = Instant.EPOCH;
    }

    Instant getLastActionTime() {
        return lastActionTime;
    }

    void recordAction(Instant now) {
        this.lastActionTime = now;
        eventTimestamps.addLast(now);
    }

    /**
     * Removes timestamps that fall outside the given window and returns
     * how many events remain within the window.
     */
    int countEventsInWindow(Instant windowStart) {
        eventTimestamps.removeIf(ts -> ts.isBefore(windowStart));
        return eventTimestamps.size();
    }
}
