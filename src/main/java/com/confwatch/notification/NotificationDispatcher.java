package com.confwatch.notification;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Dispatches NotificationEvents to one or more configured channels.
 */
public class NotificationDispatcher {

    private static final Logger logger = Logger.getLogger(NotificationDispatcher.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final List<NotificationChannel> channels;
    private final String logFilePath;
    private final String slackWebhookUrl;

    public NotificationDispatcher(List<NotificationChannel> channels, String logFilePath, String slackWebhookUrl) {
        this.channels = channels != null ? channels : List.of(NotificationChannel.CONSOLE);
        this.logFilePath = logFilePath;
        this.slackWebhookUrl = slackWebhookUrl;
    }

    public void dispatch(NotificationEvent event) {
        for (NotificationChannel channel : channels) {
            try {
                switch (channel) {
                    case CONSOLE -> dispatchToConsole(event);
                    case FILE_LOG -> dispatchToFile(event);
                    case SLACK -> dispatchToSlack(event);
                    case WEBHOOK -> logger.info("WEBHOOK channel handled by ActionExecutor for: " + event.getFilePath());
                }
            } catch (Exception e) {
                logger.warning("Failed to dispatch to channel " + channel + ": " + e.getMessage());
            }
        }
    }

    private void dispatchToConsole(NotificationEvent event) {
        System.out.println("[confwatch] " + event);
    }

    private void dispatchToFile(NotificationEvent event) throws IOException {
        if (logFilePath == null || logFilePath.isBlank()) {
            logger.warning("FILE_LOG channel configured but no logFilePath set.");
            return;
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, true))) {
            writer.println(event.toString());
        }
    }

    private void dispatchToSlack(NotificationEvent event) {
        if (slackWebhookUrl == null || slackWebhookUrl.isBlank()) {
            logger.warning("SLACK channel configured but no slackWebhookUrl set.");
            return;
        }
        String payload = String.format("{\"text\":\"confwatch: %s\"}", event);
        logger.info("Sending Slack notification to " + slackWebhookUrl + ": " + payload);
        // HTTP POST would be performed here via WebhookDispatcher or HttpClient
    }
}
