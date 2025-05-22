package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EventManagementService {

    private final EventRepository eventRepository;

    @Autowired
    public EventManagementService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public Event createEvent(String title, String description, String eventDate,
                             String location, Integer organizerId) {
        Map<String, Object> details = new HashMap<>();
        details.put("title", title);
        details.put("description", description);
        details.put("date", eventDate);
        details.put("location", location);
        details.put("organizerId", organizerId);

        UUID uuidKey = UUID.randomUUID();
        Event event = new Event(uuidKey, details);
        event.setTitle(title);
        event.setDescription(description);
        event.setEventDate(eventDate);
        event.setLocation(location);
        event.setOrganizerId(organizerId);

        return eventRepository.save(event);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public Event getEvent(UUID id) {
        return eventRepository.findById(id).orElse(null);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public Event updateEvent(UUID id, String title, String description,
                             String eventDate, String location, Integer organizerId) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            Map<String, Object> details = new HashMap<>();
            details.put("title", title);
            details.put("description", description);
            details.put("date", eventDate);
            details.put("location", location);
            details.put("organizerId", organizerId);
            event.setDetails(details);

            event.setTitle(title);
            event.setDescription(description);
            event.setEventDate(eventDate);
            event.setLocation(location);
            event.setOrganizerId(organizerId);
            event.setUpdatedAt(java.time.Instant.now());
            return eventRepository.save(event);
        }
        return null;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public void deleteEvent(UUID id) {
        eventRepository.deleteById(id);
    }

    // Optional: for test cleanup
    public void clearAllEvents() {
        eventRepository.deleteAll();
    }
}