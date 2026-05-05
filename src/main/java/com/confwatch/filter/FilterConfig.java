package com.confwatch.filter;

import java.util.Collections;
import java.util.List;

/**
 * Holds include/exclude pattern lists used to construct an {@link EventFilter}.
 * Typically populated from YAML/JSON configuration.
 */
public class FilterConfig {

    private List<String> include;
    private List<String> exclude;

    public FilterConfig() {
        this.include = Collections.emptyList();
        this.exclude = Collections.emptyList();
    }

    public FilterConfig(List<String> include, List<String> exclude) {
        this.include = include != null ? List.copyOf(include) : Collections.emptyList();
        this.exclude = exclude != null ? List.copyOf(exclude) : Collections.emptyList();
    }

    public List<String> getInclude() { return include; }
    public void setInclude(List<String> include) {
        this.include = include != null ? List.copyOf(include) : Collections.emptyList();
    }

    public List<String> getExclude() { return exclude; }
    public void setExclude(List<String> exclude) {
        this.exclude = exclude != null ? List.copyOf(exclude) : Collections.emptyList();
    }

    /** Convenience factory that builds an {@link EventFilter} from this config. */
    public EventFilter toEventFilter() {
        return new EventFilter(include, exclude);
    }

    @Override
    public String toString() {
        return "FilterConfig{include=" + include + ", exclude=" + exclude + "}";
    }
}
