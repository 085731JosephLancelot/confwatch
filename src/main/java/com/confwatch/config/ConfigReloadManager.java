package com.confwatch.config;

import com.confwatch.action.ActionExecutor;
import com.confwatch.watcher.FileWatcherService;
import com.confwatch.watcher.WatchTarget;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages hot-reloading of the confwatch configuration itself.
 * Watches the config file and re-initialises watchers when it changes.
 */
public class ConfigReloadManager {

    private static final Logger logger = Logger.getLogger(ConfigReloadManager.class.getName());

    private final String configFilePath;
    private final WatchConfigLoader loader;
    private final FileWatcherService watcherService;
    private final ActionExecutor actionExecutor;

    private volatile WatchConfig currentConfig;

    public ConfigReloadManager(String configFilePath,
                               WatchConfigLoader loader,
                               FileWatcherService watcherService,
                               ActionExecutor actionExecutor) {
        this.configFilePath = configFilePath;
        this.loader = loader;
        this.watcherService = watcherService;
        this.actionExecutor = actionExecutor;
    }

    public void initialise() throws IOException {
        currentConfig = loader.loadFromFile(configFilePath);
        applyConfig(currentConfig);
        registerSelfWatch();
        logger.info("ConfigReloadManager initialised with: " + configFilePath);
    }

    public void reload() {
        try {
            logger.info("Reloading configuration from: " + configFilePath);
            WatchConfig newConfig = loader.loadFromFile(configFilePath);
            watcherService.clearTargets();
            applyConfig(newConfig);
            currentConfig = newConfig;
            logger.info("Configuration reloaded successfully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to reload configuration", e);
        }
    }

    public WatchConfig getCurrentConfig() {
        return currentConfig;
    }

    private void applyConfig(WatchConfig config) {
        if (config.getTargets() != null) {
            for (WatchTarget target : config.getTargets()) {
                watcherService.addTarget(target);
            }
        }
    }

    private void registerSelfWatch() {
        WatchTarget selfTarget = new WatchTarget();
        selfTarget.setPath(configFilePath);
        selfTarget.setLabel("confwatch-config-self");
        watcherService.addTarget(selfTarget, path -> reload());
    }
}
