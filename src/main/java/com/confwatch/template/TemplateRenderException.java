package com.confwatch.template;

/**
 * Thrown when a template cannot be rendered due to missing or invalid variables.
 */
public class TemplateRenderException extends RuntimeException {

    public TemplateRenderException(String message) {
        super(message);
    }

    public TemplateRenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
