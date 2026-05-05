package com.confwatch.health;

import com.confwatch.config.ConfigReloadManager;
import com.confwatch.watcher.FileWatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckerTest {

    @Mock
    private FileWatcherService fileWatcherService;

    @Mock
    private ConfigReloadManager configReloadManager;

    private HealthChecker healthChecker;

    @BeforeEach
    void setUp() {
        healthChecker = new HealthChecker(fileWatcherService, configReloadManager);
    }

    @Test
    void shouldReturnUpWhenAllSubsystemsHealthy() {
        when(fileWatcherService.isRunning()).thenReturn(true);
        when(configReloadManager.isHealthy()).thenReturn(true);

        HealthStatus status = healthChecker.check();

        assertThat(status.getState()).isEqualTo(HealthStatus.State.UP);
        assertThat(status.getDetails()).containsEntry("fileWatcher", "UP");
        assertThat(status.getDetails()).containsEntry("configReloadManager", "UP");
    }

    @Test
    void shouldReturnDownWhenFileWatcherNotRunning() {
        when(fileWatcherService.isRunning()).thenReturn(false);
        when(configReloadManager.isHealthy()).thenReturn(true);

        HealthStatus status = healthChecker.check();

        assertThat(status.getState()).isEqualTo(HealthStatus.State.DOWN);
        assertThat(status.getDetails()).containsEntry("fileWatcher", "DOWN");
    }

    @Test
    void shouldReturnDownWhenConfigReloadManagerUnhealthy() {
        when(fileWatcherService.isRunning()).thenReturn(true);
        when(configReloadManager.isHealthy()).thenReturn(false);

        HealthStatus status = healthChecker.check();

        assertThat(status.getState()).isEqualTo(HealthStatus.State.DOWN);
        assertThat(status.getDetails()).containsEntry("configReloadManager", "DEGRADED");
    }

    @Test
    void shouldHandleFileWatcherException() {
        when(fileWatcherService.isRunning()).thenThrow(new RuntimeException("watcher unavailable"));
        when(configReloadManager.isHealthy()).thenReturn(true);

        HealthStatus status = healthChecker.check();

        assertThat(status.getState()).isEqualTo(HealthStatus.State.DOWN);
        assertThat(status.getDetails().get("fileWatcher")).startsWith("ERROR:");
    }

    @Test
    void shouldRejectNullFileWatcherService() {
        assertThatThrownBy(() -> new HealthChecker(null, configReloadManager))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fileWatcherService");
    }

    @Test
    void shouldRejectNullConfigReloadManager() {
        assertThatThrownBy(() -> new HealthChecker(fileWatcherService, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("configReloadManager");
    }

    @Test
    void shouldIncludeCheckedAtTimestampInDetails() {
        when(fileWatcherService.isRunning()).thenReturn(true);
        when(configReloadManager.isHealthy()).thenReturn(true);

        HealthStatus status = healthChecker.check();

        assertThat(status.getDetails()).containsKey("checkedAt");
    }
}
