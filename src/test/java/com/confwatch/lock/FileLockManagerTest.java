package com.confwatch.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FileLockManagerTest {

    private FileLockManager manager;
    private final Path testPath = Paths.get("/etc/app/config.yaml");
    private final Path otherPath = Paths.get("/etc/app/other.yaml");

    @BeforeEach
    void setUp() {
        manager = new FileLockManager(3000L);
    }

    @Test
    void constructorRejectsNonPositiveTimeout() {
        assertThrows(IllegalArgumentException.class, () -> new FileLockManager(0));
        assertThrows(IllegalArgumentException.class, () -> new FileLockManager(-1));
    }

    @Test
    void acquireAndReleaseWriteLock() {
        assertFalse(manager.isWriteLocked(testPath));
        manager.acquireWriteLock(testPath);
        assertTrue(manager.isWriteLocked(testPath));
        manager.releaseWriteLock(testPath);
        assertFalse(manager.isWriteLocked(testPath));
    }

    @Test
    void acquireAndReleaseReadLock() {
        assertDoesNotThrow(() -> {
            manager.acquireReadLock(testPath);
            manager.releaseReadLock(testPath);
        });
    }

    @Test
    void locksArePerPath() {
        manager.acquireWriteLock(testPath);
        assertFalse(manager.isWriteLocked(otherPath));
        manager.releaseWriteLock(testPath);
    }

    @Test
    void trackedPathCountIncreasesOnFirstAccess() {
        assertEquals(0, manager.trackedPathCount());
        manager.acquireWriteLock(testPath);
        assertEquals(1, manager.trackedPathCount());
        manager.acquireWriteLock(otherPath);
        assertEquals(2, manager.trackedPathCount());
        manager.releaseWriteLock(testPath);
        manager.releaseWriteLock(otherPath);
    }

    @Test
    void isStaleReturnsFalseForFreshLock() throws InterruptedException {
        manager.acquireWriteLock(testPath);
        assertFalse(manager.isStale(testPath));
        manager.releaseWriteLock(testPath);
    }

    @Test
    void isStaleReturnsFalseForUnlockedPath() {
        assertFalse(manager.isStale(testPath));
    }

    @Test
    void forceReleaseRemovesLockEntry() {
        manager.acquireWriteLock(testPath);
        assertTrue(manager.isWriteLocked(testPath));
        manager.forceRelease(testPath);
        assertFalse(manager.isWriteLocked(testPath));
    }

    @Test
    void concurrentReadLocksDoNotBlock() throws InterruptedException {
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                manager.acquireReadLock(testPath);
                successCount.incrementAndGet();
                manager.releaseReadLock(testPath);
                latch.countDown();
            }).start();
        }

        latch.await();
        assertEquals(threadCount, successCount.get());
    }
}
