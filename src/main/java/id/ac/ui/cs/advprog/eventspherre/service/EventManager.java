package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.command.Command;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.observer.UserObserver;

import java.util.HashMap;
import java.util.Map;

public class EventManager {
    private Map<Integer, Event> events = new HashMap<>();
    private int nextId = 1;

    public Event createEvent(Map<String, Object> details) {
        Event event = new Event(nextId++, details);
        events.put(event.getId(), event);
        return event;
    }

    public void executeCommand(Command command) {
        command.execute();
    }

    public void addUserToEvent(Event event, UserObserver user) {
        event.addObserver(user);
    }

    public Event getEvent(int id) {
        return events.get(id);
    }
}
