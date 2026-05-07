package com.confwatch.checkpoint;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointManagerTest {

    @TempDir
    Path tempDir;

    private CheckpointManager manager;

    @BeforeEach
    void setUp() throws IOException {
        manager = new CheckpointManager(tempDir);
    }

    @Test
    void saveAndLoad_returnsStoredEntry() throws IOException {
        manager.save("etc/app.conf", 1700000000L, "abc123");
        Optional<CheckpointEntry> result = manager.load("etc/app.conf");
        assertTrue(result.isPresent());
        assertEquals("abc123", result.get().getContentHash());
        assertEquals(1700000000L, result.get().getLastModified());
    }

    @Test
    void load_returnsEmptyForUnknownKey() {
        assertTrue(manager.load("unknown/file.conf").isEmpty());
    }

    @Test
    void hasChanged_returnsTrueWhenHashDiffers() throws IOException {
        manager.save("srv/db.yml", 100L, "hash1");
        assertTrue(manager.hasChanged("srv/db.yml", 100L, "hash2"));
    }

    @Test
    void hasChanged_returnsFalseWhenSameState() throws IOException {
        manager.save("srv/db.yml", 100L, "hashX");
        assertFalse(manager.hasChanged("srv/db.yml", 100L, "hashX"));
    }

    @Test
    void hasChanged_returnsTrueForUnseenFile() {
        assertTrue(manager.hasChanged("new/file.conf", 999L, "newhash"));
    }

    @Test
    void delete_removesEntryFromCacheAndDisk() throws IOException {
        manager.save("tmp/test.conf", 50L, "del-hash");
        assertEquals(1, manager.size());
        manager.delete("tmp/test.conf");
        assertEquals(0, manager.size());
        assertTrue(manager.load("tmp/test.conf").isEmpty());
    }

    @Test
    void reload_persistsAcrossRestarts() throws IOException {
        manager.save("persist/cfg.toml", 200L, "persisted");
        // Simulate restart by creating a new instance pointed at same dir
        CheckpointManager reloaded = new CheckpointManager(tempDir);
        Optional<CheckpointEntry> entry = reloaded.load("persist/cfg.toml");
        assertTrue(entry.isPresent());
        assertEquals("persisted", entry.get().getContentHash());
    }

    @Test
    void size_reflectsNumberOfTrackedFiles() throws IOException {
        assertEquals(0, manager.size());
        manager.save("a.conf", 1L, "h1");
        manager.save("b.conf", 2L, "h2");
        assertEquals(2, manager.size());
    }
}
