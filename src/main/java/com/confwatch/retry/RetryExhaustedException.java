package com.confwatch.retry;

/**
 * Thrown when all retry attempts for a task have been exhausted.
 */
public class RetryExhaustedException extends Exception {

    private final String taskName;
    private final int attemptsUsed;

    public RetryExhaustedException(String taskName, int attemptsUsed, Throwable cause) {
        super("Task '" + taskName + "' failed after " + attemptsUsed + " attempt(s): " +
              (cause != null ? cause.getMessage() : "unknown error"), cause);
        this.taskName = taskName;
        this.attemptsUsed = attemptsUsed;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getAttemptsUsed() {
        return attemptsUsed;
    }
}
