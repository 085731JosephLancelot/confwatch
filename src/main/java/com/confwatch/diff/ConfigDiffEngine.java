package com.confwatch.diff;

import java.util.*;

/**
 * Computes a structured diff between two versions of a config file's content.
 * Identifies added, removed, and modified lines to provide meaningful change context.
 */
public class ConfigDiffEngine {

    public enum ChangeType {
        ADDED, REMOVED, MODIFIED, UNCHANGED
    }

    public static class DiffEntry {
        private final int lineNumber;
        private final ChangeType changeType;
        private final String oldValue;
        private final String newValue;

        public DiffEntry(int lineNumber, ChangeType changeType, String oldValue, String newValue) {
            this.lineNumber = lineNumber;
            this.changeType = changeType;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public int getLineNumber() { return lineNumber; }
        public ChangeType getChangeType() { return changeType; }
        public String getOldValue() { return oldValue; }
        public String getNewValue() { return newValue; }

        @Override
        public String toString() {
            return String.format("[%d] %s: '%s' -> '%s'", lineNumber, changeType, oldValue, newValue);
        }
    }

    /**
     * Computes a line-by-line diff between old and new content.
     *
     * @param oldContent lines of the previous version
     * @param newContent lines of the current version
     * @return list of DiffEntry describing each change
     */
    public List<DiffEntry> diff(List<String> oldContent, List<String> newContent) {
        if (oldContent == null) oldContent = Collections.emptyList();
        if (newContent == null) newContent = Collections.emptyList();

        List<DiffEntry> entries = new ArrayList<>();
        int maxLines = Math.max(oldContent.size(), newContent.size());

        for (int i = 0; i < maxLines; i++) {
            boolean hasOld = i < oldContent.size();
            boolean hasNew = i < newContent.size();

            if (hasOld && hasNew) {
                String oldLine = oldContent.get(i);
                String newLine = newContent.get(i);
                if (!oldLine.equals(newLine)) {
                    entries.add(new DiffEntry(i + 1, ChangeType.MODIFIED, oldLine, newLine));
                }
            } else if (hasOld) {
                entries.add(new DiffEntry(i + 1, ChangeType.REMOVED, oldContent.get(i), null));
            } else {
                entries.add(new DiffEntry(i + 1, ChangeType.ADDED, null, newContent.get(i)));
            }
        }

        return entries;
    }

    /**
     * Returns true if there are any meaningful changes between old and new content.
     */
    public boolean hasChanges(List<String> oldContent, List<String> newContent) {
        return !diff(oldContent, newContent).isEmpty();
    }

    /**
     * Summarizes the diff as a human-readable string.
     */
    public String summarize(List<DiffEntry> entries) {
        long added = entries.stream().filter(e -> e.getChangeType() == ChangeType.ADDED).count();
        long removed = entries.stream().filter(e -> e.getChangeType() == ChangeType.REMOVED).count();
        long modified = entries.stream().filter(e -> e.getChangeType() == ChangeType.MODIFIED).count();
        return String.format("+%d added, -%d removed, ~%d modified", added, removed, modified);
    }
}
