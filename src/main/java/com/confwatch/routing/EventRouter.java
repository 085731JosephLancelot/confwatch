package com.confwatch.routing;

import com.confwatch.action.ActionConfig;
import com.confwatch.action.ActionExecutor;
import com.confwatch.filter.EventFilter;
import com.confwatch.notification.NotificationEvent;
import com.confwatch.throttle.ThrottleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Routes file change events to the appropriate actions based on registered routes.
 * Applies throttling and filtering before dispatching.
 */
public class EventRouter {

    private static final Logger logger = Logger.getLogger(EventRouter.class.getName());

    private final Map<String, List<EventRoute>> routesByPath = new ConcurrentHashMap<>();
    private final ActionExecutor actionExecutor;
    private final ThrottleManager throttleManager;

    public EventRouter(ActionExecutor actionExecutor, ThrottleManager throttleManager) {
        if (actionExecutor == null) throw new IllegalArgumentException("ActionExecutor must not be null");
        if (throttleManager == null) throw new IllegalArgumentException("ThrottleManager must not be null");
        this.actionExecutor = actionExecutor;
        this.throttleManager = throttleManager;
    }

    public void registerRoute(EventRoute route) {
        if (route == null) throw new IllegalArgumentException("Route must not be null");
        routesByPath
            .computeIfAbsent(route.getWatchPath(), k -> new ArrayList<>())
            .add(route);
        logger.info("Registered route for path: " + route.getWatchPath());
    }

    public void route(NotificationEvent event) {
        if (event == null) return;
        String path = event.getFilePath();
        List<EventRoute> routes = routesByPath.getOrDefault(path, List.of());

        if (routes.isEmpty()) {
            logger.fine("No routes registered for path: " + path);
            return;
        }

        for (EventRoute route : routes) {
            EventFilter filter = route.getFilter();
            if (filter != null && !filter.matches(event)) {
                logger.fine("Event filtered out for route on path: " + path);
                continue;
            }
            String throttleKey = route.getRouteId() + ":" + path;
            if (throttleManager.isThrottled(throttleKey)) {
                logger.warning("Event throttled for route: " + route.getRouteId());
                continue;
            }
            throttleManager.record(throttleKey);
            for (ActionConfig action : route.getActions()) {
                try {
                    actionExecutor.execute(action, event);
                } catch (Exception e) {
                    logger.severe("Failed to execute action for route " + route.getRouteId() + ": " + e.getMessage());
                }
            }
        }
    }

    public int getRouteCount() {
        return routesByPath.values().stream().mapToInt(List::size).sum();
    }

    public void clearRoutes() {
        routesByPath.clear();
    }
}
