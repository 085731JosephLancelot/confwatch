package com.confwatch.template;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateContextTest {

    @Test
    void buildsContextWithAllStandardFields() {
        Map<String, String> ctx = new TemplateContext()
            .withFilePath("/etc/nginx/nginx.conf")
            .withServiceName("nginx")
            .withEventType("CREATED")
            .withTimestamp("2024-06-01T12:00:00Z")
            .withChecksum("abc123")
            .build();

        assertEquals("/etc/nginx/nginx.conf", ctx.get("filePath"));
        assertEquals("nginx", ctx.get("serviceName"));
        assertEquals("CREATED", ctx.get("eventType"));
        assertEquals("2024-06-01T12:00:00Z", ctx.get("timestamp"));
        assertEquals("abc123", ctx.get("checksum"));
    }

    @Test
    void buildsContextWithCustomKey() {
        Map<String, String> ctx = TemplateContext.empty()
            .with("environment", "production")
            .build();
        assertEquals("production", ctx.get("environment"));
    }

    @Test
    void nullValuesAreStoredAsEmptyStrings() {
        Map<String, String> ctx = new TemplateContext()
            .withFilePath(null)
            .withServiceName(null)
            .build();
        assertEquals("", ctx.get("filePath"));
        assertEquals("", ctx.get("serviceName"));
    }

    @Test
    void throwsOnBlankCustomKey() {
        assertThrows(IllegalArgumentException.class, () ->
            TemplateContext.empty().with("", "value"));
    }

    @Test
    void throwsOnNullCustomKey() {
        assertThrows(IllegalArgumentException.class, () ->
            TemplateContext.empty().with(null, "value"));
    }

    @Test
    void builtMapIsImmutable() {
        Map<String, String> ctx = TemplateContext.empty().with("k", "v").build();
        assertThrows(UnsupportedOperationException.class, () -> ctx.put("x", "y"));
    }

    @Test
    void emptyContextBuildsEmptyMap() {
        Map<String, String> ctx = TemplateContext.empty().build();
        assertTrue(ctx.isEmpty());
    }
}
