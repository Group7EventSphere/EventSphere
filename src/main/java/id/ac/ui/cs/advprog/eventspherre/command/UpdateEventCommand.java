package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import id.ac.ui.cs.advprog.eventspherre.observer.EventSubject;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class UpdateEventCommand implements EventCommand {
    private final EventRepository eventRepository;
    private final EventSubject eventSubject;
    private final Integer eventId;
    private final String title;
    private final String description;
    private final String eventDate;
    private final String location;
    private final Integer capacity;
    private final Boolean isPublic;

    private Event originalEvent;
    private Event updatedEvent;
    private Map<String, Object> originalDetails;

    @Override
    public void execute() {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            return;
        }

        originalEvent = optionalEvent.get();

        // Store the original details for undo operation
        if (originalEvent.getDetails() != null) {
            originalDetails = new HashMap<>(originalEvent.getDetails());
        } else {
            originalDetails = new HashMap<>();
        }

        // Update the event
        Map<String, Object> details = new HashMap<>();
        details.put("title", title);
        details.put("description", description);
        details.put("date", eventDate);
        details.put("location", location);
        details.put("capacity", capacity);
        details.put("isPublic", isPublic);
        details.put("organizerId", originalEvent.getOrganizerId());

        originalEvent.setDetails(details);
        originalEvent.setTitle(title);
        originalEvent.setDescription(description);
        originalEvent.setEventDate(eventDate);
        originalEvent.setLocation(location);
        originalEvent.setCapacity(capacity);
        originalEvent.setPublic(isPublic);
        originalEvent.setUpdatedAt(Instant.now());

        updatedEvent = eventRepository.save(originalEvent);
        eventSubject.notifyEventUpdated(updatedEvent);
    }

    @Override
    public void undo() {
        if (originalEvent != null && updatedEvent != null && originalDetails != null) {
            // Restore the original event data
            Event currentEvent = eventRepository.findById(eventId).orElse(null);
            if (currentEvent != null) {
                currentEvent.setDetails(originalDetails);
                currentEvent.setTitle((String) originalDetails.get("title"));
                currentEvent.setDescription((String) originalDetails.get("description"));
                currentEvent.setEventDate((String) originalDetails.get("date"));
                currentEvent.setLocation((String) originalDetails.get("location"));
                currentEvent.setCapacity((Integer) originalDetails.get("capacity"));
                currentEvent.setPublic((Boolean) originalDetails.get("isPublic"));

                Event restoredEvent = eventRepository.save(currentEvent);
                eventSubject.notifyEventUpdated(restoredEvent);
            }
        }
    }

    public Event getEvent() {
        return updatedEvent;
    }
}
