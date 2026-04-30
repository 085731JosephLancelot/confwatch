package com.confwatch.watcher;

import java.nio.file.Path;

/**
 * Callback interface invoked when a watched config file changes.
 */
@FunctionalInterface
public interface FileChangeListener {

    /**
     * Called when a filesystem event is detected on a watched path.
     *
     * @param path      the absolute path of the changed file
     * @param eventType one of: ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE
     */
    void onFileChanged(Path path, String eventType);
}
