package id.ac.ui.cs.advprog.eventspherre.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration class for monitoring and metrics collection
 */
@Configuration
@EnableAspectJAutoProxy
public class MonitoringConfiguration {

    private final AtomicInteger activeEvents = new AtomicInteger(0);
    private final AtomicInteger totalEventCreations = new AtomicInteger(0);
    private final AtomicInteger totalEventUpdates = new AtomicInteger(0);
    private final AtomicInteger totalEventDeletions = new AtomicInteger(0);

    @Bean
    public Timer eventCreationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("event.creation.duration")
                .description("Time taken to create an event")
                .tag("operation", "create")
                .register(meterRegistry);
    }

    @Bean
    public Timer eventRetrievalTimer(MeterRegistry meterRegistry) {
        return Timer.builder("event.retrieval.duration")
                .description("Time taken to retrieve events")
                .tag("operation", "read")
                .register(meterRegistry);
    }

    @Bean
    public Timer eventUpdateTimer(MeterRegistry meterRegistry) {
        return Timer.builder("event.update.duration")
                .description("Time taken to update an event")
                .tag("operation", "update")
                .register(meterRegistry);
    }

    @Bean
    public Timer eventDeletionTimer(MeterRegistry meterRegistry) {
        return Timer.builder("event.deletion.duration")
                .description("Time taken to delete an event")
                .tag("operation", "delete")
                .register(meterRegistry);
    }

    @Bean
    public Counter eventCreationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("event.creation.total")
                .description("Total number of events created")
                .register(meterRegistry);
    }

    @Bean
    public Counter eventUpdateCounter(MeterRegistry meterRegistry) {
        return Counter.builder("event.update.total")
                .description("Total number of events updated")
                .register(meterRegistry);
    }

    @Bean
    public Counter eventDeletionCounter(MeterRegistry meterRegistry) {
        return Counter.builder("event.deletion.total")
                .description("Total number of events deleted")
                .register(meterRegistry);
    }

    @Bean
    public Counter eventRetrievalCounter(MeterRegistry meterRegistry) {
        return Counter.builder("event.retrieval.total")
                .description("Total number of event retrievals")
                .register(meterRegistry);
    }

    @Bean
    public Counter eventErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("event.error.total")
                .description("Total number of event operation errors")
                .register(meterRegistry);
    }

    @Bean
    public Gauge activeEventsGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("event.active.count")
                .description("Current number of active events")
                .register(meterRegistry, this, config -> config.activeEvents.get());
    }

    // Getter methods for the atomic counters
    public AtomicInteger getActiveEvents() {
        return activeEvents;
    }

    public AtomicInteger getTotalEventCreations() {
        return totalEventCreations;
    }

    public AtomicInteger getTotalEventUpdates() {
        return totalEventUpdates;
    }

    public AtomicInteger getTotalEventDeletions() {
        return totalEventDeletions;
    }
}
