package com.confwatch.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Executes configured actions (webhook or reload command) when a config file change is detected.
 */
public class ActionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);

    private final WebhookDispatcher webhookDispatcher;
    private final ReloadCommandRunner reloadCommandRunner;

    public ActionExecutor(WebhookDispatcher webhookDispatcher, ReloadCommandRunner reloadCommandRunner) {
        this.webhookDispatcher = webhookDispatcher;
        this.reloadCommandRunner = reloadCommandRunner;
    }

    /**
     * Executes all configured actions for the given changed file path.
     *
     * @param changedFile the path of the file that changed
     * @param config      the action configuration to apply
     */
    public void execute(Path changedFile, ActionConfig config) {
        logger.info("Executing actions for changed file: {}", changedFile);

        List<String> webhookUrls = config.getWebhookUrls();
        if (webhookUrls != null && !webhookUrls.isEmpty()) {
            for (String url : webhookUrls) {
                try {
                    webhookDispatcher.dispatch(url, changedFile);
                } catch (IOException e) {
                    logger.error("Failed to dispatch webhook to {}: {}", url, e.getMessage());
                }
            }
        }

        List<String> reloadCommands = config.getReloadCommands();
        if (reloadCommands != null && !reloadCommands.isEmpty()) {
            for (String command : reloadCommands) {
                try {
                    reloadCommandRunner.run(command, changedFile);
                } catch (IOException | InterruptedException e) {
                    logger.error("Failed to run reload command '{}': {}", command, e.getMessage());
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
