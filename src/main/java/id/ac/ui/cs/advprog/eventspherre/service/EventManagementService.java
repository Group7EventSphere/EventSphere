package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class EventManagementService {
    private final Map<UUID, Event> events = new HashMap<>();

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public Event createEvent(String title, String description, String eventDate,
                             String location, Integer organizerId) {
        // Create details map
        Map<String, Object> details = new HashMap<>();
        details.put("title", title);
        details.put("description", description);
        details.put("date", eventDate);
        details.put("location", location);
        details.put("organizerId", organizerId);

        // Generate UUID and create event
        UUID id = UUID.randomUUID();
        Event event = new Event(id, details);
        events.put(event.getId(), event);

        return event;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public Event getEvent(UUID id) {
        return events.get(id);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public List<Event> getAllEvents() {
        return new ArrayList<>(events.values());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public Event updateEvent(UUID id, String title, String description,
                             String eventDate, String location, Integer organizerId) {
        Event event = events.get(id);
        if (event != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("title", title);
            details.put("description", description);
            details.put("date", eventDate);
            details.put("location", location);
            details.put("organizerId", organizerId);
            event.setDetails(details);

            // Update the actual fields as well
            event.setTitle(title);
            event.setDescription(description);
            event.setEventDate(eventDate);
            event.setLocation(location);
            event.setOrganizerId(organizerId);
            event.setUpdatedAt(Instant.now());
        }
        return event;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public void deleteEvent(UUID id) {
        events.remove(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void clearAllEvents() {
        events.clear();
    }
}