package id.ac.ui.cs.advprog.eventspherre.model;

import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;



public class EventTest {
    private EventManagementService eventManager;

    @BeforeEach
void setUp() {
    // Create mock EventRepository
    EventRepository mockEventRepository = mock(EventRepository.class);
    
    // Track deleted event IDs
    final Set<UUID> deletedEventIds = new HashSet<>();
    
    // Configure mock behavior for save
    when(mockEventRepository.save(any(Event.class))).thenAnswer(invocation -> {
        Event event = invocation.getArgument(0);
        return event;
    });
    
    // Configure mock behavior for findById - return empty if event was deleted
    when(mockEventRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
        UUID id = invocation.getArgument(0);
        if (deletedEventIds.contains(id)) {
            return Optional.empty();
        }
        return Optional.of(new Event(id, new HashMap<>()));
    });
    
    // Configure delete to track deleted IDs
    doAnswer(invocation -> {
        UUID id = invocation.getArgument(0);
        deletedEventIds.add(id);
        return null;
    }).when(mockEventRepository).deleteById(any(UUID.class));
    
    eventManager = new EventManagementService(mockEventRepository);
}

    @Test
    void testCreateEvent() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1);
        assertNotNull(event);
        assertNotNull(event.getId());
        assertEquals("Test Event", event.getTitle());
        assertEquals("Test Description", event.getDescription());
        assertEquals("2024-12-31", event.getEventDate());
        assertEquals("Jakarta", event.getLocation());
        assertEquals(1, event.getOrganizerId());
    }

    @Test
    void testUpdateEvent() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1);
        UUID eventId = event.getId();

        Event updated = eventManager.updateEvent(
                eventId, "Updated Event", "Updated Description", "2025-01-01", "Bandung", 2);

        assertEquals("Updated Event", updated.getTitle());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals("2025-01-01", updated.getEventDate());
        assertEquals("Bandung", updated.getLocation());
        assertEquals(2, updated.getOrganizerId());
    }

    @Test
    void testGetEvent() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1);
        Event retrievedEvent = eventManager.getEvent(event.getId());
        
        // Compare IDs instead of object references
        assertEquals(event.getId(), retrievedEvent.getId());
    }

    @Test
    void testDeleteEvent() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1);
        UUID eventId = event.getId();
        eventManager.deleteEvent(eventId);
        assertNull(eventManager.getEvent(eventId));
    }
}