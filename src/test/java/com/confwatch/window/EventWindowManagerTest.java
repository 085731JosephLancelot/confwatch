package com.confwatch.window;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventWindowManagerTest {

    private EventWindowManager manager;
    private static final Duration WINDOW = Duration.ofSeconds(10);
    private static final Instant BASE = Instant.ofEpochSecond(1_000_000);

    @BeforeEach
    void setUp() {
        manager = new EventWindowManager(WINDOW);
    }

    @Test
    void constructor_rejectsNullWindowSize() {
        assertThrows(IllegalArgumentException.class, () -> new EventWindowManager(null));
    }

    @Test
    void constructor_rejectsZeroWindowSize() {
        assertThrows(IllegalArgumentException.class, () -> new EventWindowManager(Duration.ZERO));
    }

    @Test
    void addEvent_rejectsBlankFilePath() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.addEvent("  ", "MODIFIED", BASE));
    }

    @Test
    void addEvent_groupsEventsInSameWindow() {
        manager.addEvent("/etc/app.conf", "MODIFIED", BASE);
        manager.addEvent("/etc/app.conf", "MODIFIED", BASE.plusSeconds(3));

        List<WindowedEvent> events = manager.getWindowEvents("/etc/app.conf", BASE);
        assertEquals(2, events.size());
    }

    @Test
    void addEvent_separatesEventsAcrossWindows() {
        manager.addEvent("/etc/app.conf", "MODIFIED", BASE);
        manager.addEvent("/etc/app.conf", "MODIFIED", BASE.plusSeconds(15));

        List<WindowedEvent> firstWindow = manager.getWindowEvents("/etc/app.conf", BASE);
        List<WindowedEvent> secondWindow = manager.getWindowEvents("/etc/app.conf", BASE.plusSeconds(15));

        assertEquals(1, firstWindow.size());
        assertEquals(1, secondWindow.size());
        assertEquals(2, manager.activeWindowCount());
    }

    @Test
    void getWindowEvents_returnsEmptyListForUnknownWindow() {
        List<WindowedEvent> events = manager.getWindowEvents("/etc/unknown.conf", BASE);
        assertTrue(events.isEmpty());
    }

    @Test
    void flushExpiredWindows_returnsAndRemovesExpiredWindows() {
        manager.addEvent("/etc/app.conf", "MODIFIED", BASE);
        manager.addEvent("/etc/db.conf", "CREATED", BASE.plusSeconds(2));

        Instant cutoff = BASE.plusSeconds(20);
        List<EventWindow> flushed = manager.flushExpiredWindows(cutoff);

        assertEquals(2, flushed.size());
        assertEquals(0, manager.activeWindowCount());
    }

    @Test
    void flushExpiredWindows_retainsActiveWindows() {
        manager.addEvent("/etc/app.conf", "MODIFIED", BASE);
        manager.addEvent("/etc/app.conf", "MODIFIED", BASE.plusSeconds(25));

        Instant cutoff = BASE.plusSeconds(15);
        List<EventWindow> flushed = manager.flushExpiredWindows(cutoff);

        assertEquals(1, flushed.size());
        assertEquals(1, manager.activeWindowCount());
    }

    @Test
    void flushExpiredWindows_windowContainsCorrectEvents() {
        manager.addEvent("/etc/app.conf", "MODIFIED", BASE);
        manager.addEvent("/etc/app.conf", "MODIFIED", BASE.plusSeconds(4));

        List<EventWindow> flushed = manager.flushExpiredWindows(BASE.plusSeconds(20));
        assertEquals(1, flushed.size());
        assertEquals(2, flushed.get(0).events().size());
    }
}
