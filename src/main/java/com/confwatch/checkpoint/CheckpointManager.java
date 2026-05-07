package com.confwatch.checkpoint;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Manages persistent checkpoints for watched files, tracking the last
 * successfully processed state so the daemon can resume after restarts
 * without re-triggering actions for already-handled changes.
 */
public class CheckpointManager {

    private static final Logger log = Logger.getLogger(CheckpointManager.class.getName());

    private final Path checkpointDir;
    private final Map<String, CheckpointEntry> cache = new ConcurrentHashMap<>();

    public CheckpointManager(Path checkpointDir) throws IOException {
        this.checkpointDir = checkpointDir;
        Files.createDirectories(checkpointDir);
        loadAll();
    }

    public void save(String fileKey, long lastModified, String contentHash) throws IOException {
        CheckpointEntry entry = new CheckpointEntry(fileKey, lastModified, contentHash, Instant.now());
        cache.put(fileKey, entry);
        persist(entry);
        log.fine("Checkpoint saved for: " + fileKey);
    }

    public Optional<CheckpointEntry> load(String fileKey) {
        return Optional.ofNullable(cache.get(fileKey));
    }

    public boolean hasChanged(String fileKey, long lastModified, String contentHash) {
        return cache.getOrDefault(fileKey, CheckpointEntry.EMPTY)
                .isDifferentFrom(lastModified, contentHash);
    }

    public void delete(String fileKey) throws IOException {
        cache.remove(fileKey);
        Path file = checkpointFile(fileKey);
        Files.deleteIfExists(file);
    }

    private void persist(CheckpointEntry entry) throws IOException {
        Path file = checkpointFile(entry.getFileKey());
        String line = entry.getLastModified() + "|" + entry.getContentHash() + "|" + entry.getRecordedAt();
        Files.writeString(file, line);
    }

    private void loadAll() throws IOException {
        try (var stream = Files.list(checkpointDir)) {
            stream.filter(p -> p.toString().endsWith(".cp"))
                  .forEach(p -> {
                      try {
                          String raw = Files.readString(p);
                          String[] parts = raw.split("\\|", 3);
                          if (parts.length == 3) {
                              String key = p.getFileName().toString().replace(".cp", "").replace("_", "/");
                              cache.put(key, new CheckpointEntry(key,
                                      Long.parseLong(parts[0]), parts[1], Instant.parse(parts[2])));
                          }
                      } catch (IOException | NumberFormatException e) {
                          log.warning("Failed to load checkpoint: " + p + " — " + e.getMessage());
                      }
                  });
        }
        log.info("Loaded " + cache.size() + " checkpoints from " + checkpointDir);
    }

    private Path checkpointFile(String fileKey) {
        String safe = fileKey.replace("/", "_").replace("\\", "_");
        return checkpointDir.resolve(safe + ".cp");
    }

    public int size() {
        return cache.size();
    }
}
