package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class EventManagementService {

    private final EventRepository eventRepository;

    @Autowired
    public EventManagementService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public Event createEvent(String title, String description, String eventDate,
                             String location, Integer organizerId, Integer capacity, Boolean isPublic) {
        Map<String, Object> details = new HashMap<>();
        details.put("title", title);
        details.put("description", description);
        details.put("date", eventDate);
        details.put("location", location);
        details.put("organizerId", organizerId);
        details.put("capacity", capacity);
        details.put("isPublic", isPublic);

        Event event = new Event();
        event.setDetails(details);
        event.setTitle(title);
        event.setDescription(description);
        event.setEventDate(eventDate);
        event.setLocation(location);
        event.setOrganizerId(organizerId);
        event.setCapacity(capacity);  // Explicitly set capacity
        event.setPublic(isPublic);    // Explicitly set isPublic

        return eventRepository.save(event);
    }

    /**
     * Asynchronous version of createEvent method
     * This method runs in a separate thread and returns a CompletableFuture
     */
    @Async
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public CompletableFuture<Event> createEventAsync(String title, String description, String eventDate,
                             String location, Integer organizerId, Integer capacity, Boolean isPublic) {
        // Perform the synchronous operation
        Event createdEvent = createEvent(title, description, eventDate, location, organizerId, capacity, isPublic);

        // Wrap the result in a CompletableFuture
        return CompletableFuture.completedFuture(createdEvent);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public Event getEvent(int id) {
        return eventRepository.findById(id).orElse(null);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public Event updateEvent(int id, String title, String description,
                             String eventDate, String location, Integer capacity, boolean isPublic) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            Map<String, Object> details = new HashMap<>();
            details.put("title", title);
            details.put("description", description);
            details.put("date", eventDate);
            details.put("location", location);
            details.put("capacity", capacity);
            details.put("isPublic", isPublic);
            event.setDetails(details);

            event.setTitle(title);
            event.setDescription(description);
            event.setEventDate(eventDate);
            event.setLocation(location);
            event.setCapacity(capacity);
            event.setPublic(isPublic);
            event.setUpdatedAt(java.time.Instant.now());
            return eventRepository.save(event);
        }
        return null;
    }

    /**
     * Asynchronous version of updateEvent method
     * This method runs in a separate thread and returns a CompletableFuture
     */
    @Async
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public CompletableFuture<Event> updateEventAsync(int id, String title, String description,
                             String eventDate, String location, Integer capacity, boolean isPublic) {
        // Perform the synchronous operation
        Event updatedEvent = updateEvent(id, title, description, eventDate, location, capacity, isPublic);

        // Wrap the result in a CompletableFuture
        return CompletableFuture.completedFuture(updatedEvent);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public void deleteEvent(int id) {
        eventRepository.deleteById(id);
    }

    // Optional: for test cleanup
    public void clearAllEvents() {
        eventRepository.deleteAll();
    }

    public Event getEventById(Integer eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with ID: " + eventId));
    }
}

