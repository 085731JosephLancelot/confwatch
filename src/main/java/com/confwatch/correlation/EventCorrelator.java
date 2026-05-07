package com.confwatch.correlation;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Correlates related file change events within a time window,
 * grouping bursts of changes into a single correlated event group.
 */
public class EventCorrelator {

    private final CorrelationConfig config;
    private final Map<String, List<CorrelatedEvent>> pendingGroups = new ConcurrentHashMap<>();

    public EventCorrelator(CorrelationConfig config) {
        if (config == null) throw new IllegalArgumentException("CorrelationConfig must not be null");
        this.config = config;
    }

    /**
     * Submits an event for correlation. Returns a non-empty list if the event
     * completes a correlation group (window expired or max size reached).
     */
    public List<CorrelatedEvent> submit(CorrelatedEvent event) {
        if (event == null) throw new IllegalArgumentException("Event must not be null");

        String key = resolveGroupKey(event);
        pendingGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(event);

        List<CorrelatedEvent> group = pendingGroups.get(key);
        if (shouldFlush(group)) {
            pendingGroups.remove(key);
            return Collections.unmodifiableList(group);
        }
        return Collections.emptyList();
    }

    /**
     * Flushes all groups whose correlation window has expired.
     */
    public Map<String, List<CorrelatedEvent>> flushExpired() {
        Instant cutoff = Instant.now().minusMillis(config.getWindowMillis());
        Map<String, List<CorrelatedEvent>> flushed = new HashMap<>();

        pendingGroups.entrySet().removeIf(entry -> {
            List<CorrelatedEvent> group = entry.getValue();
            boolean expired = group.stream()
                    .map(CorrelatedEvent::getTimestamp)
                    .min(Comparator.naturalOrder())
                    .map(t -> t.isBefore(cutoff))
                    .orElse(false);
            if (expired) {
                flushed.put(entry.getKey(), Collections.unmodifiableList(group));
            }
            return expired;
        });
        return flushed;
    }

    public int pendingGroupCount() {
        return pendingGroups.size();
    }

    private String resolveGroupKey(CorrelatedEvent event) {
        if (config.isGroupByService() && event.getServiceName() != null) {
            return event.getServiceName();
        }
        return event.getFilePath();
    }

    private boolean shouldFlush(List<CorrelatedEvent> group) {
        return group.size() >= config.getMaxGroupSize();
    }
}
