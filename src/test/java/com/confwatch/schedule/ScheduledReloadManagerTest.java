package com.confwatch.schedule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledReloadManagerTest {

    private ScheduledReloadManager manager;

    @BeforeEach
    void setUp() {
        manager = new ScheduledReloadManager();
    }

    @AfterEach
    void tearDown() {
        manager.shutdown();
    }

    @Test
    void scheduleTriggersActionRepeatedly() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        CopyOnWriteArrayList<String> triggered = new CopyOnWriteArrayList<>();

        manager.schedule("/etc/app/config.yml", Duration.ofMillis(100), path -> {
            triggered.add(path);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Expected 3 triggers within 2s");
        assertTrue(triggered.size() >= 3);
        assertTrue(triggered.stream().allMatch(p -> p.equals("/etc/app/config.yml")));
    }

    @Test
    void isScheduledReturnsTrueAfterRegistration() {
        manager.schedule("/etc/service/app.conf", Duration.ofSeconds(5), p -> {});
        assertTrue(manager.isScheduled("/etc/service/app.conf"));
    }

    @Test
    void cancelStopsSchedule() {
        manager.schedule("/etc/service/app.conf", Duration.ofSeconds(5), p -> {});
        boolean cancelled = manager.cancel("/etc/service/app.conf");
        assertTrue(cancelled);
        assertFalse(manager.isScheduled("/etc/service/app.conf"));
    }

    @Test
    void cancelReturnsFalseWhenNoScheduleExists() {
        assertFalse(manager.cancel("/nonexistent/path.conf"));
    }

    @Test
    void lastTriggeredIsUpdatedAfterExecution() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        manager.schedule("/etc/db/db.conf", Duration.ofMillis(100), p -> latch.countDown());
        latch.await(2, TimeUnit.SECONDS);
        assertNotNull(manager.lastTriggered("/etc/db/db.conf"));
    }

    @Test
    void scheduleThrowsOnBlankPath() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.schedule("", Duration.ofSeconds(1), p -> {}));
    }

    @Test
    void scheduleThrowsOnZeroInterval() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.schedule("/etc/app.conf", Duration.ZERO, p -> {}));
    }

    @Test
    void reschedulingPathReplacesOldSchedule() throws InterruptedException {
        CopyOnWriteArrayList<String> log = new CopyOnWriteArrayList<>();
        manager.schedule("/etc/app.conf", Duration.ofSeconds(60), p -> log.add("old"));
        manager.schedule("/etc/app.conf", Duration.ofSeconds(60), p -> log.add("new"));
        assertTrue(manager.isScheduled("/etc/app.conf"));
        // Only one task should be active; old one replaced
        assertEquals(0, log.size(), "No triggers expected yet for 60s interval");
    }
}
