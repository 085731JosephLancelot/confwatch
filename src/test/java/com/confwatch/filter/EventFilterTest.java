package com.confwatch.filter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventFilterTest {

    @Test
    void acceptsAll_whenNoPatternsConfigured() {
        EventFilter filter = new EventFilter(List.of(), List.of());
        assertTrue(filter.accepts("/etc/app/config.yaml"));
        assertTrue(filter.accepts("/var/app/settings.json"));
    }

    @Test
    void rejectsNull_andBlankPaths() {
        EventFilter filter = new EventFilter(List.of(), List.of());
        assertFalse(filter.accepts(null));
        assertFalse(filter.accepts("   "));
    }

    @Test
    void includePattern_limitsAcceptedPaths() {
        EventFilter filter = new EventFilter(List.of("**.yaml"), List.of());
        assertTrue(filter.accepts("/etc/app/config.yaml"));
        assertFalse(filter.accepts("/etc/app/config.json"));
    }

    @Test
    void excludePattern_blocksMatchingPaths() {
        EventFilter filter = new EventFilter(List.of(), List.of("**/tmp/**"));
        assertFalse(filter.accepts("/var/tmp/config.yaml"));
        assertTrue(filter.accepts("/etc/app/config.yaml"));
    }

    @Test
    void excludeOverridesInclude() {
        EventFilter filter = new EventFilter(List.of("**.yaml"), List.of("**/secret/**"));
        assertFalse(filter.accepts("/etc/secret/db.yaml"));
        assertTrue(filter.accepts("/etc/app/db.yaml"));
    }

    @Test
    void globToPattern_singleStar_doesNotCrossSlash() {
        var pattern = EventFilter.globToPattern("*.yaml");
        assertTrue(pattern.matcher("config.yaml").find());
        assertFalse(pattern.matcher("dir/config.yaml").find());
    }

    @Test
    void globToPattern_doubleStar_crossesSlash() {
        var pattern = EventFilter.globToPattern("**.yaml");
        assertTrue(pattern.matcher("dir/sub/config.yaml").find());
    }

    @Test
    void multipleIncludePatterns_anyMatchAccepted() {
        EventFilter filter = new EventFilter(List.of("**.yaml", "**.json"), List.of());
        assertTrue(filter.accepts("/etc/app/config.json"));
        assertTrue(filter.accepts("/etc/app/config.yaml"));
        assertFalse(filter.accepts("/etc/app/config.toml"));
    }
}
