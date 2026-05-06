package com.confwatch.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRendererTest {

    private TemplateRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new TemplateRenderer();
    }

    @Test
    void rendersSimplePlaceholder() {
        Map<String, String> ctx = Map.of("filePath", "/etc/app/config.yml");
        String result = renderer.render("Changed: {{filePath}}", ctx);
        assertEquals("Changed: /etc/app/config.yml", result);
    }

    @Test
    void rendersMultiplePlaceholders() {
        Map<String, String> ctx = Map.of(
            "serviceName", "auth-service",
            "eventType", "MODIFIED",
            "filePath", "/etc/auth/config.yml"
        );
        String result = renderer.render(
            "[{{eventType}}] {{serviceName}} — {{filePath}}", ctx);
        assertEquals("[MODIFIED] auth-service — /etc/auth/config.yml", result);
    }

    @Test
    void returnsTemplateUnchangedWhenNoPlaceholders() {
        String result = renderer.render("No placeholders here.", Map.of());
        assertEquals("No placeholders here.", result);
    }

    @Test
    void throwsOnMissingContextKey() {
        assertThrows(TemplateRenderException.class, () ->
            renderer.render("Hello {{name}}", Map.of()));
    }

    @Test
    void throwsOnNullTemplate() {
        assertThrows(IllegalArgumentException.class, () ->
            renderer.render(null, Map.of()));
    }

    @Test
    void throwsOnNullContext() {
        assertThrows(IllegalArgumentException.class, () ->
            renderer.render("{{key}}", null));
    }

    @Test
    void hasPlaceholdersReturnsTrueForTemplate() {
        assertTrue(renderer.hasPlaceholders("Value: {{x}}"));
    }

    @Test
    void hasPlaceholdersReturnsFalseForPlainString() {
        assertFalse(renderer.hasPlaceholders("plain string"));
    }

    @Test
    void hasPlaceholdersReturnsFalseForNull() {
        assertFalse(renderer.hasPlaceholders(null));
    }

    @Test
    void rendersEmptyValueForBlankContextEntry() {
        Map<String, String> ctx = Map.of("checksum", "");
        String result = renderer.render("Checksum: {{checksum}}", ctx);
        assertEquals("Checksum: ", result);
    }
}
