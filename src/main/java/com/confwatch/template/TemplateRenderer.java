package com.confwatch.template;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders message templates by substituting named placeholders with event context values.
 * Placeholders use the syntax {{key}}.
 */
public class TemplateRenderer {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    /**
     * Renders the given template string by replacing all {{key}} placeholders
     * with values from the provided context map.
     *
     * @param template the template string
     * @param context  map of variable names to their string values
     * @return the rendered string
     * @throws TemplateRenderException if a required placeholder has no value in context
     */
    public String render(String template, Map<String, String> context) {
        if (template == null) {
            throw new IllegalArgumentException("Template must not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);
            if (!context.containsKey(key)) {
                throw new TemplateRenderException(
                    "Missing template variable: '" + key + "' in template: " + template);
            }
            String replacement = Matcher.quoteReplacement(context.get(key));
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Returns true if the template contains at least one placeholder.
     */
    public boolean hasPlaceholders(String template) {
        if (template == null) return false;
        return PLACEHOLDER_PATTERN.matcher(template).find();
    }
}
