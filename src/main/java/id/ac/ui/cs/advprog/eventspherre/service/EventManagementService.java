package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.command.CreateEventCommand;
import id.ac.ui.cs.advprog.eventspherre.command.EventCommandInvoker;
import id.ac.ui.cs.advprog.eventspherre.command.ToggleEventVisibilityCommand;
import id.ac.ui.cs.advprog.eventspherre.command.UpdateEventCommand;
import id.ac.ui.cs.advprog.eventspherre.command.DeleteEventCommand;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.observer.EventObserver;
import id.ac.ui.cs.advprog.eventspherre.observer.EventSubject;
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
    private final EventSubject eventSubject;
    private final EventCommandInvoker commandInvoker;

    @Autowired
    public EventManagementService(
            EventRepository eventRepository,
            EventSubject eventSubject,
            EventCommandInvoker commandInvoker) {
        this.eventRepository = eventRepository;
        this.eventSubject = eventSubject;
        this.commandInvoker = commandInvoker;
    }

    /**
     * Register an observer for event changes
     */
    public void registerObserver(EventObserver observer) {
        eventSubject.addObserver(observer);
    }

    /**
     * Unregister an observer
     */
    public void unregisterObserver(EventObserver observer) {
        eventSubject.removeObserver(observer);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public Event createEvent(String title, String description, String eventDate,
                             String location, Integer organizerId, Integer capacity, Boolean isPublic) {
        CreateEventCommand command = new CreateEventCommand(
                eventRepository,
                eventSubject,
                title,
                description,
                eventDate,
                location,
                organizerId,
                capacity,
                isPublic
        );
        commandInvoker.executeCommand(command);
        return command.getEvent();
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
    public Event getEventById(int id) {
        return getEvent(id);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public Event updateEvent(int id, String title, String description,
                             String eventDate, String location, Integer capacity, boolean isPublic) {
        UpdateEventCommand command = new UpdateEventCommand(
                eventRepository,
                eventSubject,
                id,
                title,
                description,
                eventDate,
                location,
                capacity,
                isPublic
        );
        commandInvoker.executeCommand(command);
        return command.getEvent();
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

    /**
     * Toggle the visibility of an event (public/private)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Event toggleEventVisibility(int id) {
        ToggleEventVisibilityCommand command = new ToggleEventVisibilityCommand(
                eventRepository,
                eventSubject,
                id
        );
        commandInvoker.executeCommand(command);
        return command.getEvent();
    }

    /**
     * Undo the last event operation
     */
    @PreAuthorize("hasRole('ADMIN')")
    public boolean undoLastOperation() {
        if (commandInvoker.hasCommandHistory()) {
            commandInvoker.undoLastCommand();
            return true;
        }
        return false;
    }

    /**
     * Delete an event by ID
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public void deleteEvent(Integer eventId) {
        DeleteEventCommand command = new DeleteEventCommand(
                eventRepository,
                eventSubject,
                eventId
        );
        commandInvoker.executeCommand(command);
    }

    /**
     * Method to find events by organizerId
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public List<Event> findEventsByOrganizerId(Integer organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    /**
     * Method to find public events
     */
    public List<Event> findPublicEvents() {
        return eventRepository.findByIsPublicTrue();
    }
}

