package com.confwatch.checkpoint;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record of a file's state at the time it was last successfully processed.
 */
public class CheckpointEntry {

    public static final CheckpointEntry EMPTY = new CheckpointEntry("", -1L, "", Instant.EPOCH);

    private final String fileKey;
    private final long lastModified;
    private final String contentHash;
    private final Instant recordedAt;

    public CheckpointEntry(String fileKey, long lastModified, String contentHash, Instant recordedAt) {
        this.fileKey = Objects.requireNonNull(fileKey, "fileKey must not be null");
        this.lastModified = lastModified;
        this.contentHash = Objects.requireNonNull(contentHash, "contentHash must not be null");
        this.recordedAt = Objects.requireNonNull(recordedAt, "recordedAt must not be null");
    }

    public String getFileKey() { return fileKey; }
    public long getLastModified() { return lastModified; }
    public String getContentHash() { return contentHash; }
    public Instant getRecordedAt() { return recordedAt; }

    public boolean isDifferentFrom(long otherModified, String otherHash) {
        return lastModified != otherModified || !contentHash.equals(otherHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckpointEntry)) return false;
        CheckpointEntry that = (CheckpointEntry) o;
        return lastModified == that.lastModified
                && Objects.equals(fileKey, that.fileKey)
                && Objects.equals(contentHash, that.contentHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileKey, lastModified, contentHash);
    }

    @Override
    public String toString() {
        return "CheckpointEntry{fileKey='" + fileKey + "', lastModified=" + lastModified
                + ", contentHash='" + contentHash + "', recordedAt=" + recordedAt + '}';
    }
}
