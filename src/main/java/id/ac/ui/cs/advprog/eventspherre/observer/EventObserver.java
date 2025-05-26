package id.ac.ui.cs.advprog.eventspherre.observer;

import id.ac.ui.cs.advprog.eventspherre.model.Event;

public interface EventObserver {
    void onEventCreated(Event event);
    void onEventUpdated(Event event);
    void onEventVisibilityChanged(Event event, boolean isPublic);
    void onEventDeleted(Event event);
}
