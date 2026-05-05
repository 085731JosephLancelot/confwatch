package com.confwatch.notification;

/**
 * Represents a notification channel type for change events.
 */
public enum NotificationChannel {
    CONSOLE,
    FILE_LOG,
    WEBHOOK,
    SLACK;

    public static NotificationChannel fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("NotificationChannel value cannot be null");
        }
        try {
            return NotificationChannel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown notification channel: " + value +
                    ". Valid values: CONSOLE, FILE_LOG, WEBHOOK, SLACK");
        }
    }
}
