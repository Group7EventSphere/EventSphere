package id.ac.ui.cs.advprog.eventspherre.monitoring;

import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for EventSphere event management system
 */
@Component
public class EventManagementHealthIndicator implements HealthIndicator {

    private final EventManagementService eventManagementService;

    @Autowired
    public EventManagementHealthIndicator(EventManagementService eventManagementService) {
        this.eventManagementService = eventManagementService;
    }

    @Override
    public Health health() {
        try {
            // Test basic functionality by attempting to get all events
            int eventCount = eventManagementService.getAllEvents().size();
            
            return Health.up()
                    .withDetail("status", "Event management system is operational")
                    .withDetail("totalEvents", eventCount)
                    .withDetail("lastChecked", System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Event management system is experiencing issues")
                    .withDetail("error", e.getMessage())
                    .withDetail("lastChecked", System.currentTimeMillis())
                    .build();
        }
    }
}
