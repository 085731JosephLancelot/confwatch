package com.confwatch.health;

import com.confwatch.watcher.FileWatcherService;
import com.confwatch.config.ConfigReloadManager;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregates health information from core subsystems and produces
 * a composite {@link HealthStatus} snapshot.
 */
public class HealthChecker {

    private final FileWatcherService fileWatcherService;
    private final ConfigReloadManager configReloadManager;

    public HealthChecker(FileWatcherService fileWatcherService,
                         ConfigReloadManager configReloadManager) {
        if (fileWatcherService == null) {
            throw new IllegalArgumentException("fileWatcherService must not be null");
        }
        if (configReloadManager == null) {
            throw new IllegalArgumentException("configReloadManager must not be null");
        }
        this.fileWatcherService = fileWatcherService;
        this.configReloadManager = configReloadManager;
    }

    /**
     * Runs all sub-checks and returns a composite health status.
     *
     * @return a {@link HealthStatus} reflecting the overall system health
     */
    public HealthStatus check() {
        Map<String, String> details = new LinkedHashMap<>();
        boolean healthy = true;

        // --- file watcher check ---
        try {
            boolean watcherRunning = fileWatcherService.isRunning();
            details.put("fileWatcher", watcherRunning ? "UP" : "DOWN");
            if (!watcherRunning) {
                healthy = false;
            }
        } catch (Exception e) {
            details.put("fileWatcher", "ERROR: " + e.getMessage());
            healthy = false;
        }

        // --- config reload manager check ---
        try {
            boolean reloadManagerOk = configReloadManager.isHealthy();
            details.put("configReloadManager", reloadManagerOk ? "UP" : "DEGRADED");
            if (!reloadManagerOk) {
                healthy = false;
            }
        } catch (Exception e) {
            details.put("configReloadManager", "ERROR: " + e.getMessage());
            healthy = false;
        }

        details.put("checkedAt", Instant.now().toString());

        return HealthStatus.of(
                healthy ? HealthStatus.State.UP : HealthStatus.State.DOWN,
                details
        );
    }
}
