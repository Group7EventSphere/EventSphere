package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.command.CreateEventCommand;
import id.ac.ui.cs.advprog.eventspherre.command.EventCommandInvoker;
import id.ac.ui.cs.advprog.eventspherre.command.ToggleEventVisibilityCommand;
import id.ac.ui.cs.advprog.eventspherre.command.UpdateEventCommand;
import id.ac.ui.cs.advprog.eventspherre.command.DeleteEventCommand;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.monitoring.ProfileExecution;
import id.ac.ui.cs.advprog.eventspherre.monitoring.EventManagementPerformanceMonitor;
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
    private final EventManagementPerformanceMonitor performanceMonitor;

    @Autowired
    public EventManagementService(
            EventRepository eventRepository,
            EventSubject eventSubject,
            EventCommandInvoker commandInvoker,
            EventManagementPerformanceMonitor performanceMonitor) {
        this.eventRepository = eventRepository;
        this.eventSubject = eventSubject;
        this.commandInvoker = commandInvoker;
        this.performanceMonitor = performanceMonitor;
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
    }    @ProfileExecution(operation = "create", logSlowExecutions = true, slowThresholdMs = 500)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public Event createEvent(String title, String description, String eventDate,
                             String location, Integer organizerId, Integer capacity, Boolean isPublic) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
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
            success = true;
            return command.getEvent();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordEventCreation(executionTime, success);
        }
    }    /**
     * Asynchronous version of createEvent method
     * This method runs in a separate thread and returns a CompletableFuture
     */
    @ProfileExecution(operation = "async_create", logSlowExecutions = true, slowThresholdMs = 1000)
    @Async
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public CompletableFuture<Event> createEventAsync(String title, String description, String eventDate,
                             String location, Integer organizerId, Integer capacity, Boolean isPublic) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            // Perform the synchronous operation
            Event createdEvent = createEvent(title, description, eventDate, location, organizerId, capacity, isPublic);
            success = true;
            // Wrap the result in a CompletableFuture
            return CompletableFuture.completedFuture(createdEvent);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordAsyncOperation("create", executionTime, success);
        }
    }    @ProfileExecution(operation = "read", logSlowExecutions = true, slowThresholdMs = 200)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public Event getEvent(int id) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            Event event = eventRepository.findById(id).orElse(null);
            success = event != null;
            return event;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordEventRetrieval(executionTime, success ? 1 : 0, success);
        }
    }

    @ProfileExecution(operation = "read", logSlowExecutions = true, slowThresholdMs = 200)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public Event getEventById(int id) {
        return getEvent(id);
    }

    @ProfileExecution(operation = "read_all", logSlowExecutions = true, slowThresholdMs = 500)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public List<Event> getAllEvents() {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            List<Event> events = eventRepository.findAll();
            success = true;
            return events;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            List<Event> events = eventRepository.findAll();
            performanceMonitor.recordEventRetrieval(executionTime, events.size(), success);
        }
    }    @ProfileExecution(operation = "update", logSlowExecutions = true, slowThresholdMs = 500)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public Event updateEvent(int id, String title, String description,
                             String eventDate, String location, Integer capacity, boolean isPublic) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
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
            success = true;
            return command.getEvent();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordEventUpdate(executionTime, success);
        }
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
    }    /**
     * Toggle the visibility of an event (public/private)
     */
    @ProfileExecution(operation = "visibility_toggle", logSlowExecutions = true, slowThresholdMs = 300)
    @PreAuthorize("hasRole('ADMIN')")
    public Event toggleEventVisibility(int id) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            ToggleEventVisibilityCommand command = new ToggleEventVisibilityCommand(
                    eventRepository,
                    eventSubject,
                    id
            );
            commandInvoker.executeCommand(command);
            success = true;
            Event event = command.getEvent();
            
            // Record visibility change metrics
            if (event != null) {
                long executionTime = System.currentTimeMillis() - startTime;
                performanceMonitor.recordEventVisibilityChange(executionTime, event.getIsPublic(), success);
            }
            
            return event;
        } finally {
            if (!success) {
                long executionTime = System.currentTimeMillis() - startTime;
                performanceMonitor.recordEventVisibilityChange(executionTime, false, success);
            }
        }
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
    }    /**
     * Delete an event by ID
     */
    @ProfileExecution(operation = "delete", logSlowExecutions = true, slowThresholdMs = 500)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public void deleteEvent(Integer eventId) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            DeleteEventCommand command = new DeleteEventCommand(
                    eventRepository,
                    eventSubject,
                    eventId
            );
            commandInvoker.executeCommand(command);
            success = true;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordEventDeletion(executionTime, success);
        }
    }

    /**
     * Method to find events by organizerId
     */
    @ProfileExecution(operation = "read_by_organizer", logSlowExecutions = true, slowThresholdMs = 300)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public List<Event> findEventsByOrganizerId(Integer organizerId) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            List<Event> events = eventRepository.findByOrganizerId(organizerId);
            success = true;
            return events;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            List<Event> events = eventRepository.findByOrganizerId(organizerId);
            performanceMonitor.recordEventRetrieval(executionTime, events != null ? events.size() : 0, success);
            performanceMonitor.recordDatabaseOperation("findByOrganizerId", executionTime, success);
        }
    }

    /**
     * Method to find public events
     */
    @ProfileExecution(operation = "read_public", logSlowExecutions = true, slowThresholdMs = 300)
    public List<Event> findPublicEvents() {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            List<Event> events = eventRepository.findByIsPublicTrue();
            success = true;
            return events;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            List<Event> events = eventRepository.findByIsPublicTrue();
            performanceMonitor.recordEventRetrieval(executionTime, events != null ? events.size() : 0, success);
            performanceMonitor.recordDatabaseOperation("findByIsPublicTrue", executionTime, success);
        }
    }
}

