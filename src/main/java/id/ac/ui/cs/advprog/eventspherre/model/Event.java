package id.ac.ui.cs.advprog.eventspherre.model;

import id.ac.ui.cs.advprog.eventspherre.observer.EventObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Event {
    private int id;
    private Map<String, Object> details;
    private List<EventObserver> observers = new ArrayList<>();

    public Event(int id, Map<String, Object> details) {
        this.id = id;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
        notifyObservers();
    }

    public void addObserver(EventObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (EventObserver observer : observers) {
            observer.update(this);
        }
    }
}
