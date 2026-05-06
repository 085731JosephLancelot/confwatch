package com.confwatch.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds a template context map from common confwatch event fields.
 * Provides a fluent API for constructing variable bindings.
 */
public class TemplateContext {

    private final Map<String, String> variables = new HashMap<>();

    public TemplateContext withFilePath(String filePath) {
        variables.put("filePath", filePath != null ? filePath : "");
        return this;
    }

    public TemplateContext withServiceName(String serviceName) {
        variables.put("serviceName", serviceName != null ? serviceName : "");
        return this;
    }

    public TemplateContext withEventType(String eventType) {
        variables.put("eventType", eventType != null ? eventType : "");
        return this;
    }

    public TemplateContext withTimestamp(String timestamp) {
        variables.put("timestamp", timestamp != null ? timestamp : "");
        return this;
    }

    public TemplateContext withChecksum(String checksum) {
        variables.put("checksum", checksum != null ? checksum : "");
        return this;
    }

    public TemplateContext with(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Context key must not be null or blank");
        }
        variables.put(key, value != null ? value : "");
        return this;
    }

    public Map<String, String> build() {
        return Collections.unmodifiableMap(new HashMap<>(variables));
    }

    public static TemplateContext empty() {
        return new TemplateContext();
    }
}
