package com.confwatch.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {

    private AuditLogger logger;

    @BeforeEach
    void setUp() {
        logger = new AuditLogger();
    }

    @Test
    void shouldRecordAndRetrieveEvents() {
        AuditEvent e1 = new AuditEvent(AuditEvent.EventType.FILE_CHANGED, "/etc/a.conf", "changed", true);
        AuditEvent e2 = new AuditEvent(AuditEvent.EventType.ACTION_TRIGGERED, "/etc/b.conf", "webhook", true);
        logger.record(e1);
        logger.record(e2);

        List<AuditEvent> events = logger.getEvents();
        assertEquals(2, events.size());
        assertTrue(events.contains(e1));
        assertTrue(events.contains(e2));
    }

    @Test
    void shouldFilterEventsByFilePath() {
        logger.record(new AuditEvent(AuditEvent.EventType.FILE_CHANGED, "/etc/a.conf", "d1", true));
        logger.record(new AuditEvent(AuditEvent.EventType.ACTION_FAILED, "/etc/b.conf", "d2", false));
        logger.record(new AuditEvent(AuditEvent.EventType.RELOAD_TRIGGERED, "/etc/a.conf", "d3", true));

        List<AuditEvent> aEvents = logger.getEventsForFile("/etc/a.conf");
        assertEquals(2, aEvents.size());
        aEvents.forEach(e -> assertEquals("/etc/a.conf", e.getFilePath()));
    }

    @Test
    void shouldClearAllEvents() {
        logger.record(new AuditEvent(AuditEvent.EventType.FILE_CHANGED, "/etc/a.conf", "d", true));
        logger.clear();
        assertTrue(logger.getEvents().isEmpty());
    }

    @Test
    void shouldPersistEventsToFile(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("audit.log");
        AuditLogger fileLogger = new AuditLogger(logFile);

        fileLogger.record(new AuditEvent(AuditEvent.EventType.FILE_CHANGED, "/etc/app.conf", "modified", true));
        fileLogger.record(new AuditEvent(AuditEvent.EventType.ACTION_TRIGGERED, "/etc/app.conf", "webhook ok", true));

        List<String> lines = Files.readAllLines(logFile);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("FILE_CHANGED"));
        assertTrue(lines.get(1).contains("ACTION_TRIGGERED"));
    }

    @Test
    void getEventsShouldReturnUnmodifiableView() {
        logger.record(new AuditEvent(AuditEvent.EventType.FILE_CHANGED, "/f", "d", true));
        List<AuditEvent> events = logger.getEvents();
        assertThrows(UnsupportedOperationException.class, () -> events.add(
                new AuditEvent(AuditEvent.EventType.ACTION_FAILED, "/f", "x", false)));
    }
}
