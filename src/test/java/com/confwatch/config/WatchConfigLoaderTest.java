package com.confwatch.config;

import com.confwatch.action.ActionConfig;
import com.confwatch.watcher.WatchTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WatchConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testLoadValidYamlConfig() throws IOException {
        String yaml = "poll_interval_ms: 3000\n" +
                "debounce_ms: 300\n" +
                "watch_targets:\n" +
                "  - path: /etc/myapp/config.yaml\n" +
                "    label: myapp-config\n" +
                "actions:\n" +
                "  - type: WEBHOOK\n" +
                "    url: http://localhost:8080/reload\n";

        Path configFile = tempDir.resolve("confwatch.yml");
        Files.writeString(configFile, yaml);

        WatchConfigLoader loader = new WatchConfigLoader();
        WatchConfig config = loader.load(configFile.toString());

        assertNotNull(config);
        assertEquals(3000L, config.getPollIntervalMs());
        assertEquals(300L, config.getDebounceMs());

        List<WatchTarget> targets = config.getWatchTargets();
        assertEquals(1, targets.size());
        assertEquals("/etc/myapp/config.yaml", targets.get(0).getPath());
        assertEquals("myapp-config", targets.get(0).getLabel());

        List<ActionConfig> actions = config.getActions();
        assertEquals(1, actions.size());
        assertEquals("WEBHOOK", actions.get(0).getType());
        assertEquals("http://localhost:8080/reload", actions.get(0).getUrl());
    }

    @Test
    void testDefaultValuesWhenFieldsOmitted() throws IOException {
        String yaml = "watch_targets:\n" +
                "  - path: /tmp/app.conf\n";

        Path configFile = tempDir.resolve("confwatch-minimal.yml");
        Files.writeString(configFile, yaml);

        WatchConfigLoader loader = new WatchConfigLoader();
        WatchConfig config = loader.load(configFile.toString());

        assertEquals(2000L, config.getPollIntervalMs());
        assertEquals(500L, config.getDebounceMs());
        assertTrue(config.getActions().isEmpty());
    }

    @Test
    void testLoadThrowsOnMissingFile() {
        WatchConfigLoader loader = new WatchConfigLoader();
        assertThrows(IOException.class, () -> loader.load("/nonexistent/path/confwatch.yml"));
    }
}
