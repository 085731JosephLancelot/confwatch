package com.confwatch.config;

import com.confwatch.action.ActionConfig;
import com.confwatch.watcher.WatchTarget;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Root configuration model loaded from confwatch.yml or confwatch.json.
 * Defines which files to watch and what actions to trigger on change.
 */
public class WatchConfig {

    @JsonProperty("watch_targets")
    private List<WatchTarget> watchTargets = new ArrayList<>();

    @JsonProperty("actions")
    private List<ActionConfig> actions = new ArrayList<>();

    @JsonProperty("poll_interval_ms")
    private long pollIntervalMs = 2000L;

    @JsonProperty("debounce_ms")
    private long debounceMs = 500L;

    public List<WatchTarget> getWatchTargets() {
        return watchTargets;
    }

    public void setWatchTargets(List<WatchTarget> watchTargets) {
        this.watchTargets = watchTargets;
    }

    public List<ActionConfig> getActions() {
        return actions;
    }

    public void setActions(List<ActionConfig> actions) {
        this.actions = actions;
    }

    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public long getDebounceMs() {
        return debounceMs;
    }

    public void setDebounceMs(long debounceMs) {
        this.debounceMs = debounceMs;
    }

    @Override
    public String toString() {
        return "WatchConfig{" +
                "watchTargets=" + watchTargets +
                ", actions=" + actions +
                ", pollIntervalMs=" + pollIntervalMs +
                ", debounceMs=" + debounceMs +
                '}';
    }
}
