package com.confwatch.throttle;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages throttle policies and state for all watch targets.
 * Determines whether an action should be allowed or suppressed based on
 * configured cooldown and event-rate limits.
 */
public class ThrottleManager {

    private final Map<String, ThrottlePolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, ThrottleState> states = new ConcurrentHashMap<>();

    /**
     * Registers or replaces the throttle policy for a target.
     */
    public void registerPolicy(ThrottlePolicy policy) {
        if (policy == null) throw new IllegalArgumentException("policy must not be null");
        policies.put(policy.getTargetId(), policy);
        states.put(policy.getTargetId(), new ThrottleState());
    }

    /**
     * Removes the throttle policy for a target (actions will always be allowed).
     */
    public void removePolicy(String targetId) {
        policies.remove(targetId);
        states.remove(targetId);
    }

    /**
     * Returns true if the action for the given target is allowed at {@code now}.
     * If allowed, the event is recorded internally.
     */
    public synchronized boolean tryAcquire(String targetId, Instant now) {
        ThrottlePolicy policy = policies.get(targetId);
        if (policy == null) {
            return true; // no policy — always allow
        }

        ThrottleState state = states.computeIfAbsent(targetId, id -> new ThrottleState());

        // Cooldown check
        Instant cooldownExpiry = state.getLastActionTime().plus(policy.getCooldown());
        if (now.isBefore(cooldownExpiry)) {
            return false;
        }

        // Rate-window check
        Instant windowStart = now.minus(policy.getWindowDuration());
        int eventsInWindow = state.countEventsInWindow(windowStart);
        if (eventsInWindow >= policy.getMaxEventsPerWindow()) {
            return false;
        }

        state.recordAction(now);
        return true;
    }

    public boolean hasPolicy(String targetId) {
        return policies.containsKey(targetId);
    }
}
