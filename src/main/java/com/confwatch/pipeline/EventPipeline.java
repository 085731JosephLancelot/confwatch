package com.confwatch.pipeline;

import com.confwatch.dedup.EventDeduplicator;
import com.confwatch.filter.EventFilter;
import com.confwatch.notification.NotificationEvent;
import com.confwatch.routing.EventRouter;
import com.confwatch.throttle.ThrottleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Orchestrates the full event processing pipeline:
 * deduplication -> filtering -> throttling -> routing -> notification dispatch.
 */
public class EventPipeline {

    private static final Logger logger = Logger.getLogger(EventPipeline.class.getName());

    private final EventDeduplicator deduplicator;
    private final EventFilter filter;
    private final ThrottleManager throttleManager;
    private final EventRouter router;
    private final List<PipelineStageResult> lastRunResults = new ArrayList<>();

    public EventPipeline(EventDeduplicator deduplicator,
                         EventFilter filter,
                         ThrottleManager throttleManager,
                         EventRouter router) {
        this.deduplicator = deduplicator;
        this.filter = filter;
        this.throttleManager = throttleManager;
        this.router = router;
    }

    /**
     * Processes an incoming notification event through all pipeline stages.
     *
     * @param event the event to process
     * @return a {@link PipelineResult} describing what happened at each stage
     */
    public PipelineResult process(NotificationEvent event) {
        lastRunResults.clear();

        if (deduplicator.isDuplicate(event)) {
            logger.fine("Event deduplicated: " + event.getEventId());
            return PipelineResult.stopped(event, PipelineStage.DEDUPLICATION);
        }
        lastRunResults.add(new PipelineStageResult(PipelineStage.DEDUPLICATION, true));

        if (!filter.accepts(event)) {
            logger.fine("Event filtered out: " + event.getEventId());
            return PipelineResult.stopped(event, PipelineStage.FILTERING);
        }
        lastRunResults.add(new PipelineStageResult(PipelineStage.FILTERING, true));

        if (throttleManager.isThrottled(event.getSourcePath())) {
            logger.fine("Event throttled: " + event.getEventId());
            return PipelineResult.stopped(event, PipelineStage.THROTTLING);
        }
        lastRunResults.add(new PipelineStageResult(PipelineStage.THROTTLING, true));

        router.route(event);
        lastRunResults.add(new PipelineStageResult(PipelineStage.ROUTING, true));

        logger.info("Event processed successfully: " + event.getEventId());
        return PipelineResult.completed(event, new ArrayList<>(lastRunResults));
    }
}
