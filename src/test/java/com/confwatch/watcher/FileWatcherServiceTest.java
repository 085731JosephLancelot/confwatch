package com.confwatch.watcher;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class FileWatcherServiceTest {

    @TempDir
    Path tempDir;

    private FileWatcherService service;

    @BeforeEach
    void setUp() throws IOException {
        // listener will be set per test
    }

    @AfterEach
    void tearDown() {
        if (service != null) {
            service.stop();
        }
    }

    @Test
    void shouldDetectFileModification() throws Exception {
        Path configFile = tempDir.resolve("app.conf");
        Files.writeString(configFile, "key=value");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> detectedEvent = new AtomicReference<>();

        service = new FileWatcherService((path, eventType) -> {
            if (path.equals(configFile)) {
                detectedEvent.set(eventType);
                latch.countDown();
            }
        });
        service.watchFile(configFile);
        service.start();

        Thread.sleep(200); // let watcher initialise
        Files.writeString(configFile, "key=updated");

        boolean triggered = latch.await(5, TimeUnit.SECONDS);
        assertTrue(triggered, "Expected file change event within timeout");
        assertEquals("ENTRY_MODIFY", detectedEvent.get());
    }

    @Test
    void shouldDetectFileDeletion() throws Exception {
        Path configFile = tempDir.resolve("delete-me.conf");
        Files.writeString(configFile, "data=1");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> detectedEvent = new AtomicReference<>();

        service = new FileWatcherService((path, eventType) -> {
            if (path.equals(configFile)) {
                detectedEvent.set(eventType);
                latch.countDown();
            }
        });
        service.watchFile(configFile);
        service.start();

        Thread.sleep(200);
        Files.delete(configFile);

        boolean triggered = latch.await(5, TimeUnit.SECONDS);
        assertTrue(triggered, "Expected delete event within timeout");
        assertEquals("ENTRY_DELETE", detectedEvent.get());
    }
}
