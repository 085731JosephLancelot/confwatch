package com.confwatch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Loads and parses the confwatch YAML configuration file.
 */
public class WatchConfigLoader {

    private static final Logger logger = Logger.getLogger(WatchConfigLoader.class.getName());
    private static final String DEFAULT_CONFIG_PATH = "confwatch.yml";

    private final ObjectMapper mapper;

    public WatchConfigLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory());
        this.mapper.findAndRegisterModules();
    }

    public WatchConfig loadFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("Config file not found: " + filePath);
        }
        logger.info("Loading configuration from: " + filePath);
        WatchConfig config = mapper.readValue(path.toFile(), WatchConfig.class);
        validate(config);
        return config;
    }

    public WatchConfig loadFromStream(InputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("Configuration input stream is null");
        }
        WatchConfig config = mapper.readValue(stream, WatchConfig.class);
        validate(config);
        return config;
    }

    public WatchConfig loadDefault() throws IOException {
        File defaultFile = new File(DEFAULT_CONFIG_PATH);
        if (defaultFile.exists()) {
            return loadFromFile(DEFAULT_CONFIG_PATH);
        }
        InputStream stream = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_PATH);
        if (stream == null) {
            throw new IOException("No default configuration found at: " + DEFAULT_CONFIG_PATH);
        }
        return loadFromStream(stream);
    }

    private void validate(WatchConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Parsed configuration is null");
        }
        if (config.getTargets() == null || config.getTargets().isEmpty()) {
            logger.warning("Configuration loaded with no watch targets defined");
        }
    }
}
