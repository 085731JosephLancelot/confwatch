package com.confwatch.snapshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages file snapshots (checksums + timestamps) to detect real content changes
 * and avoid spurious triggers on metadata-only updates.
 */
public class SnapshotManager {

    private final Map<Path, FileSnapshot> snapshots = new ConcurrentHashMap<>();

    /**
     * Records a snapshot for the given path. Returns the new snapshot.
     */
    public FileSnapshot record(Path path) throws IOException {
        String checksum = computeChecksum(path);
        Instant modifiedAt = Files.getLastModifiedTime(path).toInstant();
        FileSnapshot snapshot = new FileSnapshot(path, checksum, modifiedAt);
        snapshots.put(path, snapshot);
        return snapshot;
    }

    /**
     * Returns true if the file content has changed since the last recorded snapshot.
     * If no snapshot exists, treats the file as changed.
     */
    public boolean hasChanged(Path path) throws IOException {
        Optional<FileSnapshot> previous = Optional.ofNullable(snapshots.get(path));
        if (previous.isEmpty()) {
            return true;
        }
        String currentChecksum = computeChecksum(path);
        return !currentChecksum.equals(previous.get().getChecksum());
    }

    /**
     * Retrieves the last recorded snapshot for a path, if any.
     */
    public Optional<FileSnapshot> getSnapshot(Path path) {
        return Optional.ofNullable(snapshots.get(path));
    }

    /**
     * Removes the snapshot for the given path (e.g. when a file is deleted).
     */
    public void remove(Path path) {
        snapshots.remove(path);
    }

    /**
     * Returns the number of tracked paths.
     */
    public int size() {
        return snapshots.size();
    }

    private String computeChecksum(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(path);
            byte[] hash = digest.digest(bytes);
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
