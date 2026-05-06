package com.confwatch.watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileWatcherService {

    private static final Logger log = LoggerFactory.getLogger(FileWatcherService.class);

    private final WatchService watchService;
    private final Map<WatchKey, Path> watchedDirs = new ConcurrentHashMap<>();
    private final FileChangeListener listener;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;

    public FileWatcherService(FileChangeListener listener) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.listener = listener;
    }

    public void watchFile(Path filePath) throws IOException {
        Path dir = filePath.toAbsolutePath().getParent();
        if (dir == null) {
            throw new IllegalArgumentException("Cannot resolve parent directory for: " + filePath);
        }
        WatchKey key = dir.register(watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);
        watchedDirs.put(key, dir);
        log.info("Watching directory: {} for file: {}", dir, filePath.getFileName());
    }

    public void start() {
        running = true;
        executor.submit(this::pollEvents);
        log.info("FileWatcherService started.");
    }

    public void stop() {
        running = false;
        executor.shutdownNow();
        try {
            watchService.close();
        } catch (IOException e) {
            log.warn("Error closing WatchService", e);
        }
        log.info("FileWatcherService stopped.");
    }

    /**
     * Returns true if this service is currently running and polling for file events.
     *
     * @return {@code true} if the watcher is active, {@code false} otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the number of directories currently being watched.
     *
     * @return count of registered watch directories
     */
    public int watchedDirectoryCount() {
        return watchedDirs.size();
    }

    private void pollEvents() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                Thread.currentThread().interrupt();
                break;
            }
            Path dir = watchedDirs.get(key);
            if (dir == null) {
                key.reset();
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) continue;
                @SuppressWarnings("unchecked")
                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                Path changed = dir.resolve(pathEvent.context());
                log.debug("Detected {} on {}", kind.name(), changed);
                listener.onFileChanged(changed, kind.name());
            }
            key.reset();
        }
    }
}
