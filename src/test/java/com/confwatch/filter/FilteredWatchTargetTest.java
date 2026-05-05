package com.confwatch.filter;

import com.confwatch.watcher.WatchTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilteredWatchTargetTest {

    private WatchTarget watchTarget;

    @BeforeEach
    void setUp() {
        watchTarget = new WatchTarget("/etc/app", List.of());
    }

    @Test
    void shouldProcess_returnsTrueForMatchingPath() {
        EventFilter filter = new EventFilter(List.of("**.yaml"), List.of());
        FilteredWatchTarget fwt = new FilteredWatchTarget(watchTarget, filter);
        assertTrue(fwt.shouldProcess("/etc/app/config.yaml"));
    }

    @Test
    void shouldProcess_returnsFalseForNonMatchingPath() {
        EventFilter filter = new EventFilter(List.of("**.yaml"), List.of());
        FilteredWatchTarget fwt = new FilteredWatchTarget(watchTarget, filter);
        assertFalse(fwt.shouldProcess("/etc/app/config.json"));
    }

    @Test
    void shouldProcess_excludedPathReturnsFalse() {
        EventFilter filter = new EventFilter(List.of(), List.of("**/tmp/**"));
        FilteredWatchTarget fwt = new FilteredWatchTarget(watchTarget, filter);
        assertFalse(fwt.shouldProcess("/etc/app/tmp/config.yaml"));
    }

    @Test
    void constructor_throwsOnNullWatchTarget() {
        EventFilter filter = new EventFilter(List.of(), List.of());
        assertThrows(IllegalArgumentException.class, () -> new FilteredWatchTarget(null, filter));
    }

    @Test
    void constructor_throwsOnNullFilter() {
        assertThrows(IllegalArgumentException.class, () -> new FilteredWatchTarget(watchTarget, null));
    }

    @Test
    void getWatchTarget_returnsOriginalTarget() {
        EventFilter filter = new EventFilter(List.of(), List.of());
        FilteredWatchTarget fwt = new FilteredWatchTarget(watchTarget, filter);
        assertSame(watchTarget, fwt.getWatchTarget());
    }

    @Test
    void filterConfig_toEventFilter_integratesCorrectly() {
        FilterConfig config = new FilterConfig(List.of("**.properties"), List.of("**/test/**"));
        FilteredWatchTarget fwt = new FilteredWatchTarget(watchTarget, config.toEventFilter());
        assertTrue(fwt.shouldProcess("/etc/app/db.properties"));
        assertFalse(fwt.shouldProcess("/etc/app/test/db.properties"));
    }
}
