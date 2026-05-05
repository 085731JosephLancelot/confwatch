package com.confwatch.config;

import com.confwatch.watcher.WatchTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WatchConfigLoaderTest {

    private WatchConfigLoader loader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        loader = new WatchConfigLoader();
    }

    @Test
    void loadFromFile_parsesTargetsCorrectly() throws IOException {
        String yaml = "targets:\n" +
                "  - path: /etc/app/config.yml\n" +
                "    label: app-config\n" +
                "    debounceMs: 500\n";
        Path configFile = tempDir.resolve("confwatch.yml");
        Files.writeString(configFile, yaml);

        WatchConfig config = loader.loadFromFile(configFile.toString());

        assertNotNull(config);
        assertEquals(1, config.getTargets().size());
        WatchTarget target = config.getTargets().get(0);
        assertEquals("/etc/app/config.yml", target.getPath());
        assertEquals("app-config", target.getLabel());
    }

    @Test
    void loadFromFile_throwsWhenFileNotFound() {
        assertThrows(IOException.class, () -> loader.loadFromFile("/nonexistent/path/confwatch.yml"));
    }

    @Test
    void loadFromStream_parsesValidYaml() throws IOException {
        String yaml = "targets:\n  - path: /tmp/test.conf\n    label: test\n";
        InputStream stream = new ByteArrayInputStream(yaml.getBytes());

        WatchConfig config = loader.loadFromStream(stream);

        assertNotNull(config);
        assertFalse(config.getTargets().isEmpty());
    }

    @Test
    void loadFromStream_throwsOnNullStream() {
        assertThrows(IOException.class, () -> loader.loadFromStream(null));
    }

    @Test
    void loadFromFile_emptyTargetsLogsWarning() throws IOException {
        String yaml = "targets: []\n";
        Path configFile = tempDir.resolve("empty.yml");
        Files.writeString(configFile, yaml);

        WatchConfig config = loader.loadFromFile(configFile.toString());
        assertNotNull(config);
        assertTrue(config.getTargets().isEmpty());
    }
}
