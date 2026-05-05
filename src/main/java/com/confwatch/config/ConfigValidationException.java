package com.confwatch.config;

/**
 * Thrown when a confwatch configuration file fails validation.
 */
public class ConfigValidationException extends RuntimeException {

    private final String field;
    private final String reason;

    public ConfigValidationException(String field, String reason) {
        super("Configuration validation failed for field '" + field + "': " + reason);
        this.field = field;
        this.reason = reason;
    }

    public ConfigValidationException(String message) {
        super(message);
        this.field = null;
        this.reason = message;
    }

    public ConfigValidationException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.reason = message;
    }

    public String getField() {
        return field;
    }

    public String getReason() {
        return reason;
    }
}
