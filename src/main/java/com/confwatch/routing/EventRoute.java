package com.confwatch.routing;

import com.confwatch.action.ActionConfig;
import com.confwatch.filter.EventFilter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a routing rule that maps a watched file path to a set of actions,
 * optionally gated by an event filter.
 */
public class EventRoute {

    private final String routeId;
    private final String watchPath;
    private final List<ActionConfig> actions;
    private final EventFilter filter;

    public EventRoute(String watchPath, List<ActionConfig> actions, EventFilter filter) {
        if (watchPath == null || watchPath.isBlank()) {
            throw new IllegalArgumentException("watchPath must not be null or blank");
        }
        if (actions == null || actions.isEmpty()) {
            throw new IllegalArgumentException("actions must not be null or empty");
        }
        this.routeId = UUID.randomUUID().toString();
        this.watchPath = watchPath;
        this.actions = Collections.unmodifiableList(actions);
        this.filter = filter;
    }

    public EventRoute(String watchPath, List<ActionConfig> actions) {
        this(watchPath, actions, null);
    }

    public String getRouteId() {
        return routeId;
    }

    public String getWatchPath() {
        return watchPath;
    }

    public List<ActionConfig> getActions() {
        return actions;
    }

    public EventFilter getFilter() {
        return filter;
    }

    public boolean hasFilter() {
        return filter != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventRoute)) return false;
        EventRoute that = (EventRoute) o;
        return Objects.equals(routeId, that.routeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId);
    }

    @Override
    public String toString() {
        return "EventRoute{id='" + routeId + "', path='" + watchPath + "', actions=" + actions.size() + "}";
    }
}
