package com.confwatch.dedup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventDeduplicatorTest {

    private EventDeduplicator deduplicator;

    @BeforeEach
    void setUp() {
        deduplicator = new EventDeduplicator(500L);
    }

    @Test
    void firstEventIsNotDuplicate() {
        assertFalse(deduplicator.isDuplicate("/etc/app/config.yml:MODIFIED"));
    }

    @Test
    void sameEventWithinWindowIsDuplicate() {
        String key = "/etc/app/config.yml:MODIFIED";
        deduplicator.isDuplicate(key);
        assertTrue(deduplicator.isDuplicate(key));
    }

    @Test
    void sameEventAfterWindowIsNotDuplicate() throws InterruptedException {
        EventDeduplicator shortWindow = new EventDeduplicator(50L);
        String key = "/etc/app/config.yml:MODIFIED";
        shortWindow.isDuplicate(key);
        Thread.sleep(100);
        assertFalse(shortWindow.isDuplicate(key));
    }

    @Test
    void differentKeysAreIndependent() {
        assertFalse(deduplicator.isDuplicate("/etc/app/a.yml:MODIFIED"));
        assertFalse(deduplicator.isDuplicate("/etc/app/b.yml:MODIFIED"));
    }

    @Test
    void clearResetsAllState() {
        String key = "/etc/nginx/nginx.conf:MODIFIED";
        deduplicator.isDuplicate(key);
        deduplicator.clear();
        assertEquals(0, deduplicator.getTrackedCount());
        assertFalse(deduplicator.isDuplicate(key));
    }

    @Test
    void evictExpiredRemovesOldEntries() throws InterruptedException {
        EventDeduplicator shortWindow = new EventDeduplicator(50L);
        shortWindow.isDuplicate("/etc/app/config.yml:MODIFIED");
        assertEquals(1, shortWindow.getTrackedCount());
        Thread.sleep(100);
        shortWindow.evictExpired();
        assertEquals(0, shortWindow.getTrackedCount());
    }

    @Test
    void nullKeyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> deduplicator.isDuplicate(null));
    }

    @Test
    void blankKeyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> deduplicator.isDuplicate("  "));
    }

    @Test
    void negativeWindowThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new EventDeduplicator(-1L));
    }

    @Test
    void trackedCountReflectsUniqueKeys() {
        deduplicator.isDuplicate("key1");
        deduplicator.isDuplicate("key2");
        deduplicator.isDuplicate("key1"); // duplicate, no new entry
        assertEquals(2, deduplicator.getTrackedCount());
    }
}
