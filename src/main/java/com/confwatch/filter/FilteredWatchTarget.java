package com.confwatch.filter;

import com.confwatch.watcher.WatchTarget;

/**
 * Decorates a {@link WatchTarget} with an {@link EventFilter} so that only
 * matching file-change events are forwarded for action execution.
 */
public class FilteredWatchTarget {

    private final WatchTarget watchTarget;
    private final EventFilter eventFilter;

    public FilteredWatchTarget(WatchTarget watchTarget, EventFilter eventFilter) {
        if (watchTarget == null) throw new IllegalArgumentException("watchTarget must not be null");
        if (eventFilter == null) throw new IllegalArgumentException("eventFilter must not be null");
        this.watchTarget = watchTarget;
        this.eventFilter = eventFilter;
    }

    /**
     * Returns true when the given absolute file path should trigger actions
     * for this watch target.
     */
    public boolean shouldProcess(String absoluteFilePath) {
        return eventFilter.accepts(absoluteFilePath);
    }

    public WatchTarget getWatchTarget() { return watchTarget; }
    public EventFilter getEventFilter() { return eventFilter; }

    @Override
    public String toString() {
        return "FilteredWatchTarget{path=" + watchTarget.getPath()
                + ", filter=" + eventFilter + "}";
    }
}
