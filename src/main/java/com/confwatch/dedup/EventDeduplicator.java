package com.confwatch.dedup;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deduplicates file change events within a configurable time window.
 * Prevents redundant actions when the same file triggers multiple rapid events.
 */
public class EventDeduplicator {

    private final long windowMillis;
    private final Map<String, Instant> lastSeenMap;

    public EventDeduplicator(long windowMillis) {
        if (windowMillis <= 0) {
            throw new IllegalArgumentException("Window millis must be positive, got: " + windowMillis);
        }
        this.windowMillis = windowMillis;
        this.lastSeenMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns true if this event is a duplicate (seen within the dedup window).
     * Also records the event if it is not a duplicate.
     *
     * @param eventKey a unique key identifying the event (e.g. filePath + eventType)
     * @return true if the event should be suppressed as a duplicate
     */
    public boolean isDuplicate(String eventKey) {
        if (eventKey == null || eventKey.isBlank()) {
            throw new IllegalArgumentException("Event key must not be null or blank");
        }
        Instant now = Instant.now();
        Instant last = lastSeenMap.get(eventKey);
        if (last != null && now.toEpochMilli() - last.toEpochMilli() < windowMillis) {
            return true;
        }
        lastSeenMap.put(eventKey, now);
        return false;
    }

    /**
     * Evicts all entries older than the dedup window. Should be called periodically.
     */
    public void evictExpired() {
        Instant cutoff = Instant.ofEpochMilli(Instant.now().toEpochMilli() - windowMillis);
        lastSeenMap.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
    }

    /**
     * Clears all tracked events.
     */
    public void clear() {
        lastSeenMap.clear();
    }

    public long getWindowMillis() {
        return windowMillis;
    }

    public int getTrackedCount() {
        return lastSeenMap.size();
    }
}
