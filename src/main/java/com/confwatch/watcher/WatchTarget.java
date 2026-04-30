package com.confwatch.watcher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Represents a single config file being watched, along with its associated service name.
 */
public class WatchTarget {

    private final String serviceName;
    private final Path filePath;

    public WatchTarget(String serviceName, String filePath) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName must not be null");
        this.filePath = Paths.get(Objects.requireNonNull(filePath, "filePath must not be null"))
                             .toAbsolutePath();
    }

    public String getServiceName() {
        return serviceName;
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WatchTarget)) return false;
        WatchTarget that = (WatchTarget) o;
        return filePath.equals(that.filePath);
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    @Override
    public String toString() {
        return "WatchTarget{service='" + serviceName + "', path='" + filePath + "'}";
    }
}
