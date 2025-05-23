package id.ac.ui.cs.advprog.eventspherre.model;

import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventTest {
    private EventManagementService eventManager;

    @BeforeEach
    void setUp() {
        EventRepository mockEventRepository = mock(EventRepository.class);
        final Map<Integer, Event> eventDatabase = new HashMap<>();
        final AtomicInteger idGenerator = new AtomicInteger(1);

        // Configure mock behavior for save
        when(mockEventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event eventArg = invocation.getArgument(0);
            Event eventToStoreAndReturn = new Event();

            Integer currentId = eventArg.getId();
            if (currentId == null) {
                currentId = idGenerator.getAndIncrement();
            }
            eventToStoreAndReturn.setId(currentId);

            eventToStoreAndReturn.setTitle(eventArg.getTitle());
            eventToStoreAndReturn.setDescription(eventArg.getDescription());
            eventToStoreAndReturn.setEventDate(eventArg.getEventDate());
            eventToStoreAndReturn.setLocation(eventArg.getLocation());
            eventToStoreAndReturn.setOrganizerId(eventArg.getOrganizerId());

            // Ensure details map is initialized for the event being stored/returned by the mock
            if (eventToStoreAndReturn.getDetails() == null) {
                eventToStoreAndReturn.setDetails(new HashMap<>());
            }
            // Persist the isPublic status from eventArg into eventToStoreAndReturn's details map
            // eventArg.isPublic() will get the value from eventArg's details map.
            eventToStoreAndReturn.getDetails().put("isPublic", eventArg.isPublic());

            eventDatabase.put(currentId, eventToStoreAndReturn);
            return eventToStoreAndReturn;
        });

        // Configure mock behavior for findById
        when(mockEventRepository.findById(any(Integer.class))).thenAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            return Optional.ofNullable(eventDatabase.get(id));
        });

        // Configure mock behavior for deleteById
        doAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            eventDatabase.remove(id);
            return null;
        }).when(mockEventRepository).deleteById(any(Integer.class));

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
        assertNotNull(event);
        assertNotNull(event.getId());
        int eventId = event.getId();
        boolean originalIsPublic = event.isPublic(); // Store original isPublic status

        Event updated = eventManager.updateEvent(
                eventId, "Updated Event", "Updated Description", "2025-01-01", "Bandung", 2, originalIsPublic); // Use stored originalIsPublic
        assertNotNull(updated);
        assertEquals(eventId, updated.getId());
        assertEquals("Updated Event", updated.getTitle());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals("2025-01-01", updated.getEventDate());
        assertEquals("Bandung", updated.getLocation());
        assertEquals(2, updated.getOrganizerId());
        assertEquals(originalIsPublic, updated.isPublic()); // Assert isPublic on the 'updated' object

        Event retrievedEvent = eventManager.getEvent(eventId);
        assertNotNull(retrievedEvent);
        assertEquals("Updated Event", retrievedEvent.getTitle());
        assertEquals("Updated Description", retrievedEvent.getDescription()); // Added assertion
        assertEquals("2025-01-01", retrievedEvent.getEventDate()); // Added assertion
        assertEquals("Bandung", retrievedEvent.getLocation()); // Added assertion
        assertEquals(2, retrievedEvent.getOrganizerId()); // Added assertion
        assertEquals(originalIsPublic, retrievedEvent.isPublic()); // Added assertion for isPublic
    }

    @Test
    void testGetEvent() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1);
        assertNotNull(event);
        assertNotNull(event.getId());
        int eventId = event.getId();
        Event retrievedEvent = eventManager.getEvent(eventId);

        assertNotNull(retrievedEvent);
        assertEquals(event.getId(), retrievedEvent.getId());
        assertEquals(event.getTitle(), retrievedEvent.getTitle());
    }

    @Test
    void testDeleteEvent() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1);
        assertNotNull(event);
        assertNotNull(event.getId());
        int eventId = event.getId();

        eventManager.deleteEvent(eventId);
        Event retrievedEvent = eventManager.getEvent(eventId);
        assertNull(retrievedEvent);
    }
}

