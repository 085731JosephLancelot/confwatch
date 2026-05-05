package com.confwatch.filter;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

/**
 * Determines whether a file change event should be processed
 * based on configurable include/exclude glob/regex patterns.
 */
public class EventFilter {

    private final List<Pattern> includePatterns;
    private final List<Pattern> excludePatterns;

    public EventFilter(List<String> includeGlobs, List<String> excludeGlobs) {
        this.includePatterns = compilePatterns(includeGlobs);
        this.excludePatterns = compilePatterns(excludeGlobs);
    }

    /**
     * Returns true if the given file path should be processed.
     * A path is accepted when it matches at least one include pattern
     * (or no include patterns are configured) and does not match any
     * exclude pattern.
     */
    public boolean accepts(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        boolean included = includePatterns.isEmpty()
                || includePatterns.stream().anyMatch(p -> p.matcher(filePath).find());
        boolean excluded = excludePatterns.stream().anyMatch(p -> p.matcher(filePath).find());
        return included && !excluded;
    }

    /** Converts glob-style wildcards to a regex Pattern. */
    static Pattern globToPattern(String glob) {
        String regex = glob
                .replace(".", "\\.")
                .replace("**", "__DOUBLE_STAR__")
                .replace("*", "[^/]*")
                .replace("__DOUBLE_STAR__", ".*")
                .replace("?", "[^/]");
        return Pattern.compile(regex);
    }

    private List<Pattern> compilePatterns(List<String> globs) {
        List<Pattern> patterns = new ArrayList<>();
        if (globs != null) {
            for (String glob : globs) {
                if (glob != null && !glob.isBlank()) {
                    patterns.add(globToPattern(glob.trim()));
                }
            }
        }
        return patterns;
    }

    public List<Pattern> getIncludePatterns() { return List.copyOf(includePatterns); }
    public List<Pattern> getExcludePatterns() { return List.copyOf(excludePatterns); }
}
