package com.confwatch.lock;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Manages per-file read/write locks to prevent race conditions when multiple
 * threads detect changes on overlapping watch targets simultaneously.
 */
public class FileLockManager {

    private static final Logger logger = Logger.getLogger(FileLockManager.class.getName());

    private final Map<Path, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();
    private final Map<Path, Long> lockAcquiredAt = new ConcurrentHashMap<>();
    private final long lockTimeoutMillis;

    public FileLockManager(long lockTimeoutMillis) {
        if (lockTimeoutMillis <= 0) {
            throw new IllegalArgumentException("Lock timeout must be positive");
        }
        this.lockTimeoutMillis = lockTimeoutMillis;
    }

    public FileLockManager() {
        this(5000L);
    }

    public void acquireWriteLock(Path path) {
        ReentrantReadWriteLock lock = lockMap.computeIfAbsent(path, p -> new ReentrantReadWriteLock(true));
        lock.writeLock().lock();
        lockAcquiredAt.put(path, System.currentTimeMillis());
        logger.fine("Write lock acquired for: " + path);
    }

    public void releaseWriteLock(Path path) {
        ReentrantReadWriteLock lock = lockMap.get(path);
        if (lock != null && lock.isWriteLockedByCurrentThread()) {
            lock.writeLock().unlock();
            lockAcquiredAt.remove(path);
            logger.fine("Write lock released for: " + path);
        }
    }

    public void acquireReadLock(Path path) {
        ReentrantReadWriteLock lock = lockMap.computeIfAbsent(path, p -> new ReentrantReadWriteLock(true));
        lock.readLock().lock();
        logger.fine("Read lock acquired for: " + path);
    }

    public void releaseReadLock(Path path) {
        ReentrantReadWriteLock lock = lockMap.get(path);
        if (lock != null) {
            lock.readLock().unlock();
            logger.fine("Read lock released for: " + path);
        }
    }

    public boolean isWriteLocked(Path path) {
        ReentrantReadWriteLock lock = lockMap.get(path);
        return lock != null && lock.isWriteLocked();
    }

    public boolean isStale(Path path) {
        Long acquiredAt = lockAcquiredAt.get(path);
        return acquiredAt != null && (System.currentTimeMillis() - acquiredAt) > lockTimeoutMillis;
    }

    public void forceRelease(Path path) {
        lockMap.remove(path);
        lockAcquiredAt.remove(path);
        logger.warning("Force released all locks for: " + path);
    }

    public int trackedPathCount() {
        return lockMap.size();
    }
}
