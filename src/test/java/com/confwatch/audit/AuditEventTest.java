package com.confwatch.audit;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuditEventTest {

    @Test
    void shouldCreateEventWithCorrectFields() {
        Instant before = Instant.now();
        AuditEvent event = new AuditEvent(
                AuditEvent.EventType.FILE_CHANGED,
                "/etc/app/config.yaml",
                "File modified",
                true);
        Instant after = Instant.now();

        assertEquals(AuditEvent.EventType.FILE_CHANGED, event.getEventType());
        assertEquals("/etc/app/config.yaml", event.getFilePath());
        assertEquals("File modified", event.getDetails());
        assertTrue(event.isSuccess());
        assertFalse(event.getTimestamp().isBefore(before));
        assertFalse(event.getTimestamp().isAfter(after));
    }

    @Test
    void shouldThrowOnNullEventType() {
        assertThrows(NullPointerException.class, () ->
                new AuditEvent(null, "/etc/config.yaml", "details", true));
    }

    @Test
    void shouldThrowOnNullFilePath() {
        assertThrows(NullPointerException.class, () ->
                new AuditEvent(AuditEvent.EventType.ACTION_FAILED, null, "details", false));
    }

    @Test
    void toStringShouldContainKeyFields() {
        AuditEvent event = new AuditEvent(
                AuditEvent.EventType.ACTION_TRIGGERED,
                "/srv/nginx/nginx.conf",
                "webhook dispatched",
                true);
        String str = event.toString();
        assertTrue(str.contains("ACTION_TRIGGERED"));
        assertTrue(str.contains("/srv/nginx/nginx.conf"));
        assertTrue(str.contains("true"));
        assertTrue(str.contains("webhook dispatched"));
    }
}
