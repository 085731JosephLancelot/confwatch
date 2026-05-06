package com.confwatch.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigDiffEngineTest {

    private ConfigDiffEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ConfigDiffEngine();
    }

    @Test
    void testNoDifferencesWhenContentIsIdentical() {
        List<String> content = Arrays.asList("key=value", "timeout=30");
        List<ConfigDiffEngine.DiffEntry> diff = engine.diff(content, content);
        assertTrue(diff.isEmpty());
    }

    @Test
    void testDetectsModifiedLine() {
        List<String> oldContent = Arrays.asList("timeout=30");
        List<String> newContent = Arrays.asList("timeout=60");
        List<ConfigDiffEngine.DiffEntry> diff = engine.diff(oldContent, newContent);
        assertEquals(1, diff.size());
        assertEquals(ConfigDiffEngine.ChangeType.MODIFIED, diff.get(0).getChangeType());
        assertEquals("timeout=30", diff.get(0).getOldValue());
        assertEquals("timeout=60", diff.get(0).getNewValue());
        assertEquals(1, diff.get(0).getLineNumber());
    }

    @Test
    void testDetectsAddedLine() {
        List<String> oldContent = Arrays.asList("key=value");
        List<String> newContent = Arrays.asList("key=value", "newkey=newval");
        List<ConfigDiffEngine.DiffEntry> diff = engine.diff(oldContent, newContent);
        assertEquals(1, diff.size());
        assertEquals(ConfigDiffEngine.ChangeType.ADDED, diff.get(0).getChangeType());
        assertNull(diff.get(0).getOldValue());
        assertEquals("newkey=newval", diff.get(0).getNewValue());
    }

    @Test
    void testDetectsRemovedLine() {
        List<String> oldContent = Arrays.asList("key=value", "removeme=true");
        List<String> newContent = Arrays.asList("key=value");
        List<ConfigDiffEngine.DiffEntry> diff = engine.diff(oldContent, newContent);
        assertEquals(1, diff.size());
        assertEquals(ConfigDiffEngine.ChangeType.REMOVED, diff.get(0).getChangeType());
        assertEquals("removeme=true", diff.get(0).getOldValue());
        assertNull(diff.get(0).getNewValue());
    }

    @Test
    void testHandlesNullOldContent() {
        List<String> newContent = Arrays.asList("key=value");
        List<ConfigDiffEngine.DiffEntry> diff = engine.diff(null, newContent);
        assertEquals(1, diff.size());
        assertEquals(ConfigDiffEngine.ChangeType.ADDED, diff.get(0).getChangeType());
    }

    @Test
    void testHandlesNullNewContent() {
        List<String> oldContent = Arrays.asList("key=value");
        List<ConfigDiffEngine.DiffEntry> diff = engine.diff(oldContent, null);
        assertEquals(1, diff.size());
        assertEquals(ConfigDiffEngine.ChangeType.REMOVED, diff.get(0).getChangeType());
    }

    @Test
    void testHasChangesReturnsTrueWhenDifferent() {
        assertTrue(engine.hasChanges(
            Arrays.asList("a=1"),
            Arrays.asList("a=2")
        ));
    }

    @Test
    void testHasChangesReturnsFalseWhenSame() {
        List<String> content = Arrays.asList("a=1", "b=2");
        assertFalse(engine.hasChanges(content, content));
    }

    @Test
    void testSummarizeProducesCorrectCounts() {
        List<String> oldContent = Arrays.asList("a=1", "b=2", "c=3");
        List<String> newContent = Arrays.asList("a=1", "b=99", "c=3", "d=4");
        List<ConfigDiffEngine.DiffEntry> diff = engine.diff(oldContent, newContent);
        String summary = engine.summarize(diff);
        assertEquals("+1 added, -0 removed, ~1 modified", summary);
    }
}
