package id.ac.ui.cs.advprog.eventspherre.observer;

import id.ac.ui.cs.advprog.eventspherre.model.Event;


public class UserObserver implements EventObserver {
    private String username;

    public UserObserver(String username) {
        this.username = username;
    }

    @Override
    public void onEventCreated(Event event) {
        System.out.println(username + " received notification: Event created - " + event.getTitle() + " (ID: " + event.getId() + ")");
    }

    @Override
    public void onEventUpdated(Event event) {
        System.out.println(username + " received notification: Event updated - " + event.getTitle() + " (ID: " + event.getId() + ")");
    }

    @Override
    public void onEventVisibilityChanged(Event event, boolean isPublic) {
        String visibility = isPublic ? "public" : "private";
        System.out.println(username + " received notification: Event visibility changed to " + visibility + " - " + event.getTitle() + " (ID: " + event.getId() + ")");
    }

    @Override
    public void onEventDeleted(Event event) {
        System.out.println(username + " received notification: Event deleted - " + event.getTitle() + " (ID: " + event.getId() + ")");
    }
}

