package com.confwatch.snapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotManagerTest {

    @TempDir
    Path tempDir;

    private SnapshotManager snapshotManager;

    @BeforeEach
    void setUp() {
        snapshotManager = new SnapshotManager();
    }

    @Test
    void recordCreatesSnapshotWithChecksum() throws IOException {
        Path file = tempDir.resolve("app.conf");
        Files.writeString(file, "key=value");

        FileSnapshot snapshot = snapshotManager.record(file);

        assertNotNull(snapshot);
        assertNotNull(snapshot.getChecksum());
        assertFalse(snapshot.getChecksum().isEmpty());
        assertEquals(file, snapshot.getPath());
    }

    @Test
    void hasChangedReturnsTrueWhenNoSnapshotExists() throws IOException {
        Path file = tempDir.resolve("app.conf");
        Files.writeString(file, "key=value");

        assertTrue(snapshotManager.hasChanged(file));
    }

    @Test
    void hasChangedReturnsFalseWhenContentUnchanged() throws IOException {
        Path file = tempDir.resolve("app.conf");
        Files.writeString(file, "key=value");
        snapshotManager.record(file);

        assertFalse(snapshotManager.hasChanged(file));
    }

    @Test
    void hasChangedReturnsTrueAfterContentModified() throws IOException {
        Path file = tempDir.resolve("app.conf");
        Files.writeString(file, "key=value");
        snapshotManager.record(file);

        Files.writeString(file, "key=new_value");

        assertTrue(snapshotManager.hasChanged(file));
    }

    @Test
    void getSnapshotReturnsEmptyWhenNotRecorded() {
        Path file = tempDir.resolve("missing.conf");
        Optional<FileSnapshot> result = snapshotManager.getSnapshot(file);
        assertTrue(result.isEmpty());
    }

    @Test
    void removeDeletesSnapshot() throws IOException {
        Path file = tempDir.resolve("app.conf");
        Files.writeString(file, "key=value");
        snapshotManager.record(file);
        assertEquals(1, snapshotManager.size());

        snapshotManager.remove(file);

        assertEquals(0, snapshotManager.size());
        assertTrue(snapshotManager.getSnapshot(file).isEmpty());
    }

    @Test
    void differentFilesHaveDifferentChecksums() throws IOException {
        Path file1 = tempDir.resolve("a.conf");
        Path file2 = tempDir.resolve("b.conf");
        Files.writeString(file1, "alpha=1");
        Files.writeString(file2, "beta=2");

        FileSnapshot s1 = snapshotManager.record(file1);
        FileSnapshot s2 = snapshotManager.record(file2);

        assertNotEquals(s1.getChecksum(), s2.getChecksum());
        assertEquals(2, snapshotManager.size());
    }
}
