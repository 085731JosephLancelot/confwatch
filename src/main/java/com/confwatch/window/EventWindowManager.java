package com.confwatch.window;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages tumbling time windows for grouping file change events.
 * Events within the same window are batched together for downstream processing.
 */
public class EventWindowManager {

    private final Duration windowSize;
    private final Map<String, List<WindowedEvent>> windows = new ConcurrentHashMap<>();

    public EventWindowManager(Duration windowSize) {
        if (windowSize == null || windowSize.isNegative() || windowSize.isZero()) {
            throw new IllegalArgumentException("Window size must be a positive duration");
        }
        this.windowSize = windowSize;
    }

    /**
     * Adds an event to the appropriate window bucket keyed by file path.
     *
     * @param filePath the path of the changed config file
     * @param eventType the type of change event (e.g. MODIFIED, CREATED, DELETED)
     * @param timestamp the time at which the event occurred
     */
    public void addEvent(String filePath, String eventType, Instant timestamp) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be null or blank");
        }
        WindowedEvent event = new WindowedEvent(filePath, eventType, timestamp);
        windows.computeIfAbsent(windowKey(filePath, timestamp), k -> new ArrayList<>()).add(event);
    }

    /**
     * Returns all events for a given file path that fall within the window containing the given timestamp.
     */
    public List<WindowedEvent> getWindowEvents(String filePath, Instant timestamp) {
        String key = windowKey(filePath, timestamp);
        return windows.getOrDefault(key, List.of());
    }

    /**
     * Flushes and returns all completed windows whose end time is before the given cutoff.
     */
    public List<EventWindow> flushExpiredWindows(Instant cutoff) {
        List<EventWindow> flushed = new ArrayList<>();
        for (Map.Entry<String, List<WindowedEvent>> entry : windows.entrySet()) {
            List<WindowedEvent> events = entry.getValue();
            if (events.isEmpty()) continue;
            Instant windowStart = windowStart(events.get(0).timestamp());
            Instant windowEnd = windowStart.plus(windowSize);
            if (windowEnd.isBefore(cutoff)) {
                flushed.add(new EventWindow(windowStart, windowEnd, List.copyOf(events)));
                windows.remove(entry.getKey());
            }
        }
        return flushed;
    }

    /**
     * Returns the total number of tracked window buckets.
     */
    public int activeWindowCount() {
        return windows.size();
    }

    private String windowKey(String filePath, Instant timestamp) {
        long epochSeconds = windowStart(timestamp).getEpochSecond();
        return filePath + "@" + epochSeconds;
    }

    private Instant windowStart(Instant timestamp) {
        long secs = timestamp.getEpochSecond();
        long windowSecs = windowSize.getSeconds();
        long aligned = (secs / windowSecs) * windowSecs;
        return Instant.ofEpochSecond(aligned);
    }
}
