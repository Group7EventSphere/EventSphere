package id.ac.ui.cs.advprog.eventspherre.observer;

import id.ac.ui.cs.advprog.eventspherre.model.Event;


public class UserObserver implements EventObserver {
    private String username;

    public UserObserver(String username) {
        this.username = username;
    }

    @Override
    public void update(Event event) {
        System.out.println(username + " received an update for event " + event.getId() + ": " + event.getDetails());
    }
}