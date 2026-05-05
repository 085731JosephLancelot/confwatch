package com.confwatch.config;

import com.confwatch.action.ActionExecutor;
import com.confwatch.watcher.FileWatcherService;
import com.confwatch.watcher.WatchTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigReloadManagerTest {

    @Mock
    private WatchConfigLoader loader;
    @Mock
    private FileWatcherService watcherService;
    @Mock
    private ActionExecutor actionExecutor;

    private ConfigReloadManager manager;
    private WatchConfig sampleConfig;

    @BeforeEach
    void setUp() {
        manager = new ConfigReloadManager("confwatch.yml", loader, watcherService, actionExecutor);
        WatchTarget target = new WatchTarget();
        target.setPath("/etc/app/config.yml");
        target.setLabel("app-config");
        sampleConfig = new WatchConfig();
        sampleConfig.setTargets(List.of(target));
    }

    @Test
    void initialise_loadsConfigAndRegistersTargets() throws IOException {
        when(loader.loadFromFile("confwatch.yml")).thenReturn(sampleConfig);

        manager.initialise();

        verify(loader).loadFromFile("confwatch.yml");
        verify(watcherService, atLeastOnce()).addTarget(any(WatchTarget.class));
        assertEquals(sampleConfig, manager.getCurrentConfig());
    }

    @Test
    void reload_clearsAndReappliesTargets() throws IOException {
        when(loader.loadFromFile("confwatch.yml")).thenReturn(sampleConfig);
        manager.initialise();

        WatchConfig updatedConfig = new WatchConfig();
        WatchTarget newTarget = new WatchTarget();
        newTarget.setPath("/etc/app/new.yml");
        newTarget.setLabel("new-config");
        updatedConfig.setTargets(List.of(newTarget));
        when(loader.loadFromFile("confwatch.yml")).thenReturn(updatedConfig);

        manager.reload();

        verify(watcherService).clearTargets();
        assertEquals(updatedConfig, manager.getCurrentConfig());
    }

    @Test
    void reload_handlesIOExceptionGracefully() throws IOException {
        when(loader.loadFromFile("confwatch.yml")).thenReturn(sampleConfig);
        manager.initialise();

        when(loader.loadFromFile("confwatch.yml")).thenThrow(new IOException("disk error"));

        assertDoesNotThrow(() -> manager.reload());
        assertEquals(sampleConfig, manager.getCurrentConfig());
    }
}
