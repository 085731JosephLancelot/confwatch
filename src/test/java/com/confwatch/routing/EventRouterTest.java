package com.confwatch.routing;

import com.confwatch.action.ActionConfig;
import com.confwatch.action.ActionExecutor;
import com.confwatch.notification.NotificationEvent;
import com.confwatch.throttle.ThrottleManager;
import com.confwatch.throttle.ThrottlePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventRouterTest {

    private ActionExecutor actionExecutor;
    private ThrottleManager throttleManager;
    private EventRouter router;

    @BeforeEach
    void setUp() {
        actionExecutor = mock(ActionExecutor.class);
        throttleManager = mock(ThrottleManager.class);
        router = new EventRouter(actionExecutor, throttleManager);
    }

    @Test
    void constructorRejectsNullActionExecutor() {
        assertThrows(IllegalArgumentException.class,
            () -> new EventRouter(null, throttleManager));
    }

    @Test
    void constructorRejectsNullThrottleManager() {
        assertThrows(IllegalArgumentException.class,
            () -> new EventRouter(actionExecutor, null));
    }

    @Test
    void registerRouteIncreasesCount() {
        ActionConfig action = mock(ActionConfig.class);
        EventRoute route = new EventRoute("/etc/app.conf", List.of(action));
        router.registerRoute(route);
        assertEquals(1, router.getRouteCount());
    }

    @Test
    void registerRouteRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> router.registerRoute(null));
    }

    @Test
    void routeDispatchesActionWhenNotThrottled() throws Exception {
        ActionConfig action = mock(ActionConfig.class);
        EventRoute route = new EventRoute("/etc/app.conf", List.of(action));
        router.registerRoute(route);

        NotificationEvent event = mock(NotificationEvent.class);
        when(event.getFilePath()).thenReturn("/etc/app.conf");
        when(throttleManager.isThrottled(anyString())).thenReturn(false);

        router.route(event);

        verify(actionExecutor, times(1)).execute(eq(action), eq(event));
        verify(throttleManager, times(1)).record(anyString());
    }

    @Test
    void routeSkipsActionWhenThrottled() throws Exception {
        ActionConfig action = mock(ActionConfig.class);
        EventRoute route = new EventRoute("/etc/app.conf", List.of(action));
        router.registerRoute(route);

        NotificationEvent event = mock(NotificationEvent.class);
        when(event.getFilePath()).thenReturn("/etc/app.conf");
        when(throttleManager.isThrottled(anyString())).thenReturn(true);

        router.route(event);

        verify(actionExecutor, never()).execute(any(), any());
    }

    @Test
    void routeDoesNothingForUnregisteredPath() throws Exception {
        NotificationEvent event = mock(NotificationEvent.class);
        when(event.getFilePath()).thenReturn("/unknown/path.conf");

        router.route(event);

        verify(actionExecutor, never()).execute(any(), any());
    }

    @Test
    void clearRoutesResetsCount() {
        ActionConfig action = mock(ActionConfig.class);
        router.registerRoute(new EventRoute("/etc/a.conf", List.of(action)));
        router.registerRoute(new EventRoute("/etc/b.conf", List.of(action)));
        router.clearRoutes();
        assertEquals(0, router.getRouteCount());
    }

    @Test
    void routeNullEventDoesNotThrow() {
        assertDoesNotThrow(() -> router.route(null));
    }
}
