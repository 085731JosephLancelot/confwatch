package com.confwatch.notification;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventTest {

    @Test
    void testEventCreationAndGetters() {
        Instant before = Instant.now();
        NotificationEvent event = new NotificationEvent("/etc/app/config.yml", "MODIFIED", "app-service");
        Instant after = Instant.now();

        assertEquals("/etc/app/config.yml", event.getFilePath());
        assertEquals("MODIFIED", event.getChangeType());
        assertEquals("app-service", event.getServiceLabel());
        assertFalse(event.getTimestamp().isBefore(before));
        assertFalse(event.getTimestamp().isAfter(after));
    }

    @Test
    void testNullServiceLabelDefaultsToUnknown() {
        NotificationEvent event = new NotificationEvent("/etc/app/config.yml", "CREATED", null);
        assertEquals("unknown", event.getServiceLabel());
    }

    @Test
    void testNullFilePathThrows() {
        assertThrows(NullPointerException.class,
                () -> new NotificationEvent(null, "MODIFIED", "svc"));
    }

    @Test
    void testNullChangeTypeThrows() {
        assertThrows(NullPointerException.class,
                () -> new NotificationEvent("/etc/app/config.yml", null, "svc"));
    }

    @Test
    void testToStringContainsKeyInfo() {
        NotificationEvent event = new NotificationEvent("/etc/nginx/nginx.conf", "MODIFIED", "nginx");
        String str = event.toString();
        assertTrue(str.contains("nginx.conf"));
        assertTrue(str.contains("MODIFIED"));
        assertTrue(str.contains("nginx"));
    }

    @Test
    void testNotificationChannelFromString() {
        assertEquals(NotificationChannel.CONSOLE, NotificationChannel.fromString("console"));
        assertEquals(NotificationChannel.SLACK, NotificationChannel.fromString("SLACK"));
        assertEquals(NotificationChannel.FILE_LOG, NotificationChannel.fromString("file_log"));
    }

    @Test
    void testNotificationChannelFromStringInvalidThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> NotificationChannel.fromString("EMAIL"));
        assertThrows(IllegalArgumentException.class,
                () -> NotificationChannel.fromString(null));
    }
}
