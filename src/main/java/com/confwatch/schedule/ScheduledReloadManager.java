package com.confwatch.schedule;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Manages scheduled (periodic) reload triggers for watched config files.
 * Allows registering file paths with a reload interval and a callback action.
 */
public class ScheduledReloadManager {

    private static final Logger log = Logger.getLogger(ScheduledReloadManager.class.getName());

    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastTriggered = new ConcurrentHashMap<>();

    public ScheduledReloadManager() {
        this(Executors.newScheduledThreadPool(4));
    }

    public ScheduledReloadManager(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Registers a periodic reload for the given path at the specified interval.
     * If a schedule already exists for the path, it is replaced.
     *
     * @param path     the config file path to reload
     * @param interval the interval between reload triggers
     * @param action   the callback to invoke on each trigger
     */
    public void schedule(String path, Duration interval, Consumer<String> action) {
        if (path == null || path.isBlank()) throw new IllegalArgumentException("Path must not be blank");
        if (interval == null || interval.isNegative() || interval.isZero())
            throw new IllegalArgumentException("Interval must be positive");

        cancel(path);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                log.info("Scheduled reload triggered for: " + path);
                lastTriggered.put(path, Instant.now());
                action.accept(path);
            } catch (Exception e) {
                log.warning("Error during scheduled reload for " + path + ": " + e.getMessage());
            }
        }, interval.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);

        scheduledTasks.put(path, future);
        log.info("Scheduled reload registered for: " + path + " every " + interval);
    }

    /**
     * Cancels the scheduled reload for the given path.
     *
     * @param path the config file path
     * @return true if a schedule was cancelled, false if none existed
     */
    public boolean cancel(String path) {
        ScheduledFuture<?> existing = scheduledTasks.remove(path);
        if (existing != null) {
            existing.cancel(false);
            log.info("Cancelled scheduled reload for: " + path);
            return true;
        }
        return false;
    }

    public boolean isScheduled(String path) {
        ScheduledFuture<?> f = scheduledTasks.get(path);
        return f != null && !f.isCancelled() && !f.isDone();
    }

    public Instant lastTriggered(String path) {
        return lastTriggered.get(path);
    }

    public void shutdown() {
        scheduler.shutdownNow();
        scheduledTasks.clear();
    }
}
