package id.ac.ui.cs.advprog.eventspherre.observer;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UserObserver implements EventObserver {
    private static final Logger logger = LoggerFactory.getLogger(UserObserver.class);
    private static final String ID_SUFFIX = " (ID: ";
    
    private String username;

    public UserObserver(String username) {
        this.username = username;
    }    @Override
    public void onEventCreated(Event event) {
        logger.info("{} received notification: Event created - {}{}{}", username, event.getTitle(), ID_SUFFIX, event.getId() + ")");
    }

    @Override
    public void onEventUpdated(Event event) {
        logger.info("{} received notification: Event updated - {}{}{}", username, event.getTitle(), ID_SUFFIX, event.getId() + ")");
    }

    @Override
    public void onEventVisibilityChanged(Event event, boolean isPublic) {
        String visibility = isPublic ? AppConstants.VISIBILITY_PUBLIC : AppConstants.VISIBILITY_PRIVATE;
        logger.info("{} received notification: Event visibility changed to {} - {}{}{}", username, visibility, event.getTitle(), ID_SUFFIX, event.getId() + ")");
    }

    @Override
    public void onEventDeleted(Event event) {
        logger.info("{} received notification: Event deleted - {}{}{}", username, event.getTitle(), ID_SUFFIX, event.getId() + ")");
    }
}

