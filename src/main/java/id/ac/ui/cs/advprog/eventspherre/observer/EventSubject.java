package id.ac.ui.cs.advprog.eventspherre.observer;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventSubject {
    private final List<EventObserver> observers = new ArrayList<>();

    public void addObserver(EventObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(EventObserver observer) {
        observers.remove(observer);
    }

    public void notifyEventCreated(Event event) {
        for (EventObserver observer : observers) {
            observer.onEventCreated(event);
        }
    }

    public void notifyEventUpdated(Event event) {
        for (EventObserver observer : observers) {
            observer.onEventUpdated(event);
        }
    }

    public void notifyEventVisibilityChanged(Event event, boolean isPublic) {
        for (EventObserver observer : observers) {
            observer.onEventVisibilityChanged(event, isPublic);
        }
    }

    public void notifyEventDeleted(Event event) {
        for (EventObserver observer : observers) {
            observer.onEventDeleted(event);
        }
    }
}
