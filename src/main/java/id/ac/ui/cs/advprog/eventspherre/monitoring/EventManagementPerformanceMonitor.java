package id.ac.ui.cs.advprog.eventspherre.monitoring;

import id.ac.ui.cs.advprog.eventspherre.config.MonitoringConfiguration;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for monitoring event management performance and metrics
 */
@Service
public class EventManagementPerformanceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(EventManagementPerformanceMonitor.class);

    private final MeterRegistry meterRegistry;
    private final MonitoringConfiguration monitoringConfig;
    private final Counter eventCreationCounter;
    private final Counter eventUpdateCounter;
    private final Counter eventDeletionCounter;
    private final Counter eventRetrievalCounter;
    private final Counter eventErrorCounter;
    private final Timer eventCreationTimer;
    private final Timer eventUpdateTimer;
    private final Timer eventDeletionTimer;
    private final Timer eventRetrievalTimer;

    @Autowired
    public EventManagementPerformanceMonitor(
            MeterRegistry meterRegistry,
            MonitoringConfiguration monitoringConfig,
            Counter eventCreationCounter,
            Counter eventUpdateCounter,
            Counter eventDeletionCounter,
            Counter eventRetrievalCounter,
            Counter eventErrorCounter,
            Timer eventCreationTimer,
            Timer eventUpdateTimer,
            Timer eventDeletionTimer,
            Timer eventRetrievalTimer) {
        
        this.meterRegistry = meterRegistry;
        this.monitoringConfig = monitoringConfig;
        this.eventCreationCounter = eventCreationCounter;
        this.eventUpdateCounter = eventUpdateCounter;
        this.eventDeletionCounter = eventDeletionCounter;
        this.eventRetrievalCounter = eventRetrievalCounter;
        this.eventErrorCounter = eventErrorCounter;
        this.eventCreationTimer = eventCreationTimer;
        this.eventUpdateTimer = eventUpdateTimer;
        this.eventDeletionTimer = eventDeletionTimer;
        this.eventRetrievalTimer = eventRetrievalTimer;
    }

    /**
     * Record event creation metrics
     */
    public void recordEventCreation(long executionTimeMs, boolean success) {
        eventCreationCounter.increment();
        eventCreationTimer.record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        if (success) {
            monitoringConfig.getActiveEvents().incrementAndGet();
            monitoringConfig.getTotalEventCreations().incrementAndGet();
            logger.debug("Event creation recorded: {}ms, Total active events: {}", 
                executionTimeMs, monitoringConfig.getActiveEvents().get());
        } else {
            eventErrorCounter.increment();
            logger.warn("Failed event creation recorded: {}ms", executionTimeMs);
        }
    }

    /**
     * Record event update metrics
     */
    public void recordEventUpdate(long executionTimeMs, boolean success) {
        eventUpdateCounter.increment();
        eventUpdateTimer.record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        if (success) {
            monitoringConfig.getTotalEventUpdates().incrementAndGet();
            logger.debug("Event update recorded: {}ms", executionTimeMs);
        } else {
            eventErrorCounter.increment();
            logger.warn("Failed event update recorded: {}ms", executionTimeMs);
        }
    }

    /**
     * Record event deletion metrics
     */
    public void recordEventDeletion(long executionTimeMs, boolean success) {
        eventDeletionCounter.increment();
        eventDeletionTimer.record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        if (success) {
            monitoringConfig.getActiveEvents().decrementAndGet();
            monitoringConfig.getTotalEventDeletions().incrementAndGet();
            logger.debug("Event deletion recorded: {}ms, Total active events: {}", 
                executionTimeMs, monitoringConfig.getActiveEvents().get());
        } else {
            eventErrorCounter.increment();
            logger.warn("Failed event deletion recorded: {}ms", executionTimeMs);
        }
    }

    /**
     * Record event retrieval metrics
     */
    public void recordEventRetrieval(long executionTimeMs, int resultCount, boolean success) {
        eventRetrievalCounter.increment();
        eventRetrievalTimer.record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        if (success) {
            // Record additional metrics for retrieval operations
            Counter.builder("event.retrieval.results")
                    .tag("result_count", resultCount > 0 ? "non_empty" : "empty")
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Event retrieval recorded: {}ms, {} results", executionTimeMs, resultCount);
        } else {
            eventErrorCounter.increment();
            logger.warn("Failed event retrieval recorded: {}ms", executionTimeMs);
        }
    }

    /**
     * Record event visibility change metrics
     */
    public void recordEventVisibilityChange(long executionTimeMs, boolean isPublic, boolean success) {
        Counter.builder("event.visibility.change.total")
                .tag("visibility", isPublic ? "public" : "private")
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();

        Timer.builder("event.visibility.change.duration")
                .tag("visibility", isPublic ? "public" : "private")
                .register(meterRegistry)
                .record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        if (success) {
            logger.debug("Event visibility change recorded: {}ms, visibility: {}", 
                executionTimeMs, isPublic ? "public" : "private");
        } else {
            eventErrorCounter.increment();
            logger.warn("Failed event visibility change recorded: {}ms", executionTimeMs);
        }
    }

    /**
     * Record database operation metrics
     */
    public void recordDatabaseOperation(String operation, long executionTimeMs, boolean success) {
        Counter.builder("event.database.operation.total")
                .tag("operation", operation)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();

        Timer.builder("event.database.operation.duration")
                .tag("operation", operation)
                .register(meterRegistry)
                .record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        if (!success) {
            eventErrorCounter.increment();
        }

        logger.debug("Database {} operation recorded: {}ms, success: {}", operation, executionTimeMs, success);
    }

    /**
     * Record async operation metrics
     */
    public void recordAsyncOperation(String operation, long executionTimeMs, boolean success) {
        Counter.builder("event.async.operation.total")
                .tag("operation", operation)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();

        Timer.builder("event.async.operation.duration")
                .tag("operation", operation)
                .register(meterRegistry)
                .record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        if (!success) {
            eventErrorCounter.increment();
        }

        logger.debug("Async {} operation recorded: {}ms, success: {}", operation, executionTimeMs, success);
    }

    /**
     * Get current performance statistics
     */
    public PerformanceStats getCurrentStats() {
        return new PerformanceStats(
                monitoringConfig.getActiveEvents().get(),
                monitoringConfig.getTotalEventCreations().get(),
                monitoringConfig.getTotalEventUpdates().get(),
                monitoringConfig.getTotalEventDeletions().get(),
                eventCreationCounter.count(),
                eventUpdateCounter.count(),
                eventDeletionCounter.count(),
                eventRetrievalCounter.count(),
                eventErrorCounter.count()
        );
    }

    /**
     * Performance statistics holder
     */
    public static class PerformanceStats {
        public final int activeEvents;
        public final int totalCreations;
        public final int totalUpdates;
        public final int totalDeletions;
        public final double creationRequests;
        public final double updateRequests;
        public final double deletionRequests;
        public final double retrievalRequests;
        public final double errorCount;

        public PerformanceStats(int activeEvents, int totalCreations, int totalUpdates, int totalDeletions,
                              double creationRequests, double updateRequests, double deletionRequests,
                              double retrievalRequests, double errorCount) {
            this.activeEvents = activeEvents;
            this.totalCreations = totalCreations;
            this.totalUpdates = totalUpdates;
            this.totalDeletions = totalDeletions;
            this.creationRequests = creationRequests;
            this.updateRequests = updateRequests;
            this.deletionRequests = deletionRequests;
            this.retrievalRequests = retrievalRequests;
            this.errorCount = errorCount;
        }

        @Override
        public String toString() {
            return String.format(
                "PerformanceStats{activeEvents=%d, totalCreations=%d, totalUpdates=%d, totalDeletions=%d, " +
                "creationRequests=%.0f, updateRequests=%.0f, deletionRequests=%.0f, retrievalRequests=%.0f, errorCount=%.0f}",
                activeEvents, totalCreations, totalUpdates, totalDeletions,
                creationRequests, updateRequests, deletionRequests, retrievalRequests, errorCount
            );
        }
    }
}
