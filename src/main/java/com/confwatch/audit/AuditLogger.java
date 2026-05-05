package com.confwatch.audit;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Records AuditEvents in-memory and optionally persists them to a log file.
 */
public class AuditLogger {

    private static final Logger log = Logger.getLogger(AuditLogger.class.getName());

    private final List<AuditEvent> eventLog = new CopyOnWriteArrayList<>();
    private final Path logFilePath;

    /** Creates an AuditLogger that only keeps events in memory. */
    public AuditLogger() {
        this.logFilePath = null;
    }

    /** Creates an AuditLogger that also appends events to the given file. */
    public AuditLogger(Path logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void record(AuditEvent event) {
        eventLog.add(event);
        log.info(event.toString());
        if (logFilePath != null) {
            persistToFile(event);
        }
    }

    public List<AuditEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<>(eventLog));
    }

    public List<AuditEvent> getEventsForFile(String filePath) {
        List<AuditEvent> result = new ArrayList<>();
        for (AuditEvent e : eventLog) {
            if (e.getFilePath().equals(filePath)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void clear() {
        eventLog.clear();
    }

    private void persistToFile(AuditEvent event) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                logFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(event.toString());
            writer.newLine();
        } catch (IOException e) {
            log.warning("Failed to write audit event to file: " + e.getMessage());
        }
    }
}
