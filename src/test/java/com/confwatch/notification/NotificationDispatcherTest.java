package com.confwatch.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationDispatcherTest {

    @TempDir
    Path tempDir;

    @Test
    void testConsoleChannelDoesNotThrow() {
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                List.of(NotificationChannel.CONSOLE), null, null);
        NotificationEvent event = new NotificationEvent("/etc/app/config.yml", "MODIFIED", "app-service");
        assertDoesNotThrow(() -> dispatcher.dispatch(event));
    }

    @Test
    void testFileLogChannelWritesEntry() throws IOException {
        Path logFile = tempDir.resolve("confwatch.log");
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                List.of(NotificationChannel.FILE_LOG), logFile.toString(), null);
        NotificationEvent event = new NotificationEvent("/etc/nginx/nginx.conf", "MODIFIED", "nginx");

        dispatcher.dispatch(event);

        assertTrue(Files.exists(logFile), "Log file should be created");
        String content = Files.readString(logFile);
        assertTrue(content.contains("nginx.conf"), "Log should contain file name");
        assertTrue(content.contains("nginx"), "Log should contain service label");
    }

    @Test
    void testFileLogWithNoPathDoesNotThrow() {
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                List.of(NotificationChannel.FILE_LOG), null, null);
        NotificationEvent event = new NotificationEvent("/etc/app/config.yml", "CREATED", "app");
        assertDoesNotThrow(() -> dispatcher.dispatch(event));
    }

    @Test
    void testSlackWithNoUrlDoesNotThrow() {
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                List.of(NotificationChannel.SLACK), null, null);
        NotificationEvent event = new NotificationEvent("/etc/app/config.yml", "DELETED", "app");
        assertDoesNotThrow(() -> dispatcher.dispatch(event));
    }

    @Test
    void testMultipleChannels() throws IOException {
        Path logFile = tempDir.resolve("multi.log");
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                List.of(NotificationChannel.CONSOLE, NotificationChannel.FILE_LOG),
                logFile.toString(), null);
        NotificationEvent event = new NotificationEvent("/etc/redis/redis.conf", "MODIFIED", "redis");

        assertDoesNotThrow(() -> dispatcher.dispatch(event));
        assertTrue(Files.exists(logFile));
    }

    @Test
    void testNullChannelListDefaultsToConsole() {
        NotificationDispatcher dispatcher = new NotificationDispatcher(null, null, null);
        NotificationEvent event = new NotificationEvent("/etc/app/config.yml", "MODIFIED", "app");
        assertDoesNotThrow(() -> dispatcher.dispatch(event));
    }
}
