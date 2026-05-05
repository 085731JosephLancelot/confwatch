package com.confwatch.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Dispatches HTTP POST webhooks when a config file change is detected.
 */
public class WebhookDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(WebhookDispatcher.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    /**
     * Sends an HTTP POST to the given URL with a JSON payload describing the changed file.
     *
     * @param webhookUrl  the target URL
     * @param changedFile the file that triggered the event
     * @throws IOException if the HTTP request fails
     */
    public void dispatch(String webhookUrl, Path changedFile) throws IOException {
        logger.info("Dispatching webhook to {} for file {}", webhookUrl, changedFile);

        String payload = buildPayload(changedFile);
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                logger.info("Webhook dispatched successfully to {} (HTTP {})", webhookUrl, responseCode);
            } else {
                logger.warn("Webhook to {} returned non-2xx status: {}", webhookUrl, responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }

    private String buildPayload(Path changedFile) {
        return "{\"event\":\"FILE_CHANGED\",\"file\":\"" + changedFile.toAbsolutePath() +
               "\",\"timestamp\":\"" + Instant.now() + "\"}";
    }
}
