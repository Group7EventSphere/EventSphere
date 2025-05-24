package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import id.ac.ui.cs.advprog.eventspherre.observer.EventSubject;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class CreateEventCommand implements EventCommand {
    private final EventRepository eventRepository;
    private final EventSubject eventSubject;
    private final String title;
    private final String description;
    private final String eventDate;
    private final String location;
    private final Integer organizerId;
    private final Integer capacity;
    private final Boolean isPublic;

    private Event createdEvent;

    @Override
    public void execute() {
        Event event = new Event();
        Map<String, Object> details = new HashMap<>();

        details.put("title", title);
        details.put("description", description);
        details.put("date", eventDate);
        details.put("location", location);
        details.put("organizerId", organizerId);
        details.put("capacity", capacity);
        details.put("isPublic", isPublic);

        event.setDetails(details);
        event.setTitle(title);
        event.setDescription(description);
        event.setEventDate(eventDate);
        event.setLocation(location);
        event.setOrganizerId(organizerId);
        event.setCapacity(capacity);
        event.setPublic(isPublic);
        event.setCreatedAt(Instant.now());

        createdEvent = eventRepository.save(event);
        eventSubject.notifyEventCreated(createdEvent);
    }

    @Override
    public void undo() {
        if (createdEvent != null) {
            eventRepository.delete(createdEvent);
            eventSubject.notifyEventDeleted(createdEvent);
            createdEvent = null;
        }
    }

    public Event getEvent() {
        return createdEvent;
    }
}
