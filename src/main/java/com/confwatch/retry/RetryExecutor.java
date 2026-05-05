package com.confwatch.retry;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes a callable with retry logic based on a {@link RetryPolicy}.
 */
public class RetryExecutor {

    private static final Logger logger = Logger.getLogger(RetryExecutor.class.getName());

    private final RetryPolicy policy;

    public RetryExecutor(RetryPolicy policy) {
        if (policy == null) throw new IllegalArgumentException("RetryPolicy must not be null");
        this.policy = policy;
    }

    /**
     * Executes the given task, retrying on failure according to the policy.
     *
     * @param task     the task to execute
     * @param taskName a descriptive name used for logging
     * @param <T>      return type
     * @return the result of the task
     * @throws RetryExhaustedException if all attempts fail
     */
    public <T> T execute(Callable<T> task, String taskName) throws RetryExhaustedException {
        Exception lastException = null;
        for (int attempt = 1; attempt <= policy.getMaxAttempts(); attempt++) {
            long delay = policy.delayForAttempt(attempt);
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(taskName, attempt, ie);
                }
            }
            try {
                T result = task.call();
                if (attempt > 1) {
                    logger.info("[RetryExecutor] '" + taskName + "' succeeded on attempt " + attempt);
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                logger.log(Level.WARNING,
                    "[RetryExecutor] '" + taskName + "' failed on attempt " + attempt +
                    " of " + policy.getMaxAttempts() + ": " + e.getMessage());
            }
        }
        throw new RetryExhaustedException(taskName, policy.getMaxAttempts(), lastException);
    }

    public RetryPolicy getPolicy() {
        return policy;
    }
}
