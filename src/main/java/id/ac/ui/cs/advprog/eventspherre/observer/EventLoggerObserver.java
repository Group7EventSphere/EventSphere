package id.ac.ui.cs.advprog.eventspherre.observer;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventLoggerObserver implements EventObserver {
    private final Logger logger;

    @Autowired
    public EventLoggerObserver() {
        this.logger = LoggerFactory.getLogger(EventLoggerObserver.class);
    }

    // Constructor for testing
    EventLoggerObserver(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onEventCreated(Event event) {
        logger.info("Event created: {} (ID: {})", event.getTitle(), event.getId());
    }

    @Override
    public void onEventUpdated(Event event) {
        logger.info("Event updated: {} (ID: {})", event.getTitle(), event.getId());
    }

    @Override
    public void onEventVisibilityChanged(Event event, boolean isPublic) {
        logger.info("Event visibility changed: {} (ID: {}) is now {}",
                event.getTitle(), event.getId(), isPublic ? AppConstants.VISIBILITY_PUBLIC : AppConstants.VISIBILITY_PRIVATE);
    }

    @Override
    public void onEventDeleted(Event event) {
        logger.info("Event deleted: {} (ID: {})", event.getTitle(), event.getId());
    }
}
