package id.ac.ui.cs.advprog.eventspherre.model;

import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.observer.EventSubject;
import id.ac.ui.cs.advprog.eventspherre.command.EventCommandInvoker;
import id.ac.ui.cs.advprog.eventspherre.command.EventCommand;
import id.ac.ui.cs.advprog.eventspherre.command.CreateEventCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
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
    private EventRepository mockEventRepository;
    private EventSubject mockEventSubject;
    private EventCommandInvoker mockCommandInvoker;
    private EventManagementService eventManager;
    private Map<Integer, Event> eventDatabase;
    private Integer currentId;

    @BeforeEach
    void setUp() {
        mockEventRepository = mock(EventRepository.class);
        mockEventSubject = mock(EventSubject.class);
        mockCommandInvoker = mock(EventCommandInvoker.class);
        eventDatabase = new HashMap<>();
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
            eventToStoreAndReturn.setOrganizerId(eventArg.getOrganizerId());            // Explicitly set capacity field from source event
            eventToStoreAndReturn.setCapacity(eventArg.getCapacity());

            // Explicitly set isPublic field from source event
            eventToStoreAndReturn.setPublic(eventArg.isPublic());

            // Ensure details map is initialized for the event being stored/returned by the mock
            if (eventToStoreAndReturn.getDetails() == null) {
                eventToStoreAndReturn.setDetails(new HashMap<>());
            }

            // Copy capacity to details map for backward compatibility
            eventToStoreAndReturn.getDetails().put("capacity", eventArg.getCapacity());

            // Persist the isPublic status from eventArg into eventToStoreAndReturn's details map
            if (eventArg.getDetails() != null && eventArg.getDetails().containsKey("isPublic")) {
                eventToStoreAndReturn.getDetails().put("isPublic", eventArg.getDetails().get("isPublic"));
            } else {
                eventToStoreAndReturn.getDetails().put("isPublic", eventArg.isPublic());
            }

            eventDatabase.put(currentId, eventToStoreAndReturn);
            return eventToStoreAndReturn;
        });

        // Configure mock behavior for findById
        when(mockEventRepository.findById(any(Integer.class))).thenAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            return Optional.ofNullable(eventDatabase.get(id));
        });        // Configure mock behavior for deleteById
        doAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            eventDatabase.remove(id);
            return null;
        }).when(mockEventRepository).deleteById(any(Integer.class));

        // Configure mock behavior for delete(Event)
        doAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            if (event != null && event.getId() != null) {
                eventDatabase.remove(event.getId());
            }
            return null;
        }).when(mockEventRepository).delete(any(Event.class));

        // Configure mockCommandInvoker to actually execute commands
        doAnswer(invocation -> {
            Object command = invocation.getArgument(0);
            if (command instanceof EventCommand) {
                ((EventCommand) command).execute();
            }
            return null;
        }).when(mockCommandInvoker).executeCommand(any());

        eventManager = new EventManagementService(mockEventRepository, mockEventSubject, mockCommandInvoker);
    }

    @Test
    void testCreateEvent() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1, 100, true);
        assertNotNull(event);
        assertNotNull(event.getId());
        assertEquals("Test Event", event.getTitle());
        assertEquals("Test Description", event.getDescription());
        assertEquals("2024-12-31", event.getEventDate());
        assertEquals("Jakarta", event.getLocation());
        assertEquals(1, event.getOrganizerId());
        assertEquals(100, event.getCapacity());
        assertTrue(event.isPublic());
    }

    @Test
    void testUpdateEvent() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1, 100, true);
        assertNotNull(event);
        assertNotNull(event.getId());
        int eventId = event.getId();
        boolean originalIsPublic = event.isPublic(); // Store original isPublic status

        Event updated = eventManager.updateEvent(
                eventId, "Updated Event", "Updated Description", "2025-01-01", "Bandung", 200, originalIsPublic); // Updated to use capacity instead of organizerId
        assertNotNull(updated);
        assertEquals(eventId, updated.getId());
        assertEquals("Updated Event", updated.getTitle());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals("2025-01-01", updated.getEventDate());
        assertEquals("Bandung", updated.getLocation());
        assertEquals(200, updated.getCapacity());  // Updated assertion to check capacity
        assertEquals(originalIsPublic, updated.isPublic());

        Event retrievedEvent = eventManager.getEvent(eventId);
        assertNotNull(retrievedEvent);
        assertEquals("Updated Event", retrievedEvent.getTitle());
        assertEquals("Updated Description", retrievedEvent.getDescription());
        assertEquals("2025-01-01", retrievedEvent.getEventDate());
        assertEquals("Bandung", retrievedEvent.getLocation());
        assertEquals(200, retrievedEvent.getCapacity());  // Updated assertion to check capacity
        assertEquals(originalIsPublic, retrievedEvent.isPublic());
    }

    @Test
    void testGetEvent() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1, 100, true);
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
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1, 100, true);
        assertNotNull(event);
        assertNotNull(event.getId());
        int eventId = event.getId();

        eventManager.deleteEvent(eventId);
        Event retrievedEvent = eventManager.getEvent(eventId);
        assertNull(retrievedEvent);
    }

    @Test
    void testCreateEventWithZeroCapacity() {
        Event event = eventManager.createEvent(
                "Zero Capacity Event", "Description", "2024-12-31", "Jakarta", 1, 0, true);
        assertNotNull(event);
        assertEquals(0, event.getCapacity());
    }

    @Test
    void testToggleEventVisibility() {
        // Create public event
        Event event = eventManager.createEvent(
                "Public Event", "Description", "2024-12-31", "Jakarta", 1, 100, true);
        assertTrue(event.isPublic());

        // Update to private
        Event updated = eventManager.updateEvent(
                event.getId(), event.getTitle(), event.getDescription(),
                event.getEventDate(), event.getLocation(), event.getCapacity(), false);
        assertFalse(updated.isPublic());
    }

    @Test
    void testGetNonExistentEvent() {
        Event retrievedEvent = eventManager.getEvent(999);
        assertNull(retrievedEvent);
    }

    @Test
    void testCreateEventWithNullDescription() {
        Event event = eventManager.createEvent(
                "Event with Null Description", null, "2024-12-31", "Jakarta", 1, 100, true);
        assertNotNull(event);
        assertNull(event.getDescription());
    }

    @Test
    void testSetCapacityUpdatesDetailsMap() {
        Event event = new Event();
        event.setCapacity(250);
        assertEquals(250, event.getCapacity());
        assertEquals(250, event.getDetails().get("capacity"));
    }

    @Test
    void testSetPublicUpdatesDetailsMap() {
        Event event = new Event();
        event.setPublic(true);
        assertTrue(event.isPublic());
        assertTrue((Boolean) event.getDetails().get("isPublic"));

        // Test toggling to false
        event.setPublic(false);
        assertFalse(event.isPublic());
        assertFalse((Boolean) event.getDetails().get("isPublic"));
    }

    @Test
    void testDefaultCapacity() {
        Event event = new Event();
        assertNull(event.getCapacity());
    }

    @Test
    void testDefaultPublicStatus() {
        Event event = new Event();
        assertFalse(event.isPublic());
    }

    @Test
    void testSetPublicFalse() {
        Event event = new Event();
        event.setPublic(false);
        assertFalse(event.isPublic());
        assertEquals(false, event.getDetails().get("isPublic"));
    }

    @Test
    void testUpdateEventCapacityOnly() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1, 100, true);
        assertNotNull(event);
        int eventId = event.getId();

        Event updated = eventManager.updateEvent(
                eventId, event.getTitle(), event.getDescription(),
                event.getEventDate(), event.getLocation(), 150, event.isPublic());

        assertEquals(150, updated.getCapacity());
        // Verify other fields remain unchanged
        assertEquals(event.getTitle(), updated.getTitle());
        assertEquals(event.getDescription(), updated.getDescription());
        assertEquals(event.getEventDate(), updated.getEventDate());
        assertEquals(event.getLocation(), updated.getLocation());
        assertEquals(event.isPublic(), updated.isPublic());
    }

    @Test
    void testUpdateEventVisibilityOnly() {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1, 100, true);
        assertNotNull(event);
        int eventId = event.getId();

        Event updated = eventManager.updateEvent(
                eventId, event.getTitle(), event.getDescription(),
                event.getEventDate(), event.getLocation(), event.getCapacity(), false);

        assertFalse(updated.isPublic());
        // Verify other fields remain unchanged
        assertEquals(event.getTitle(), updated.getTitle());
        assertEquals(event.getDescription(), updated.getDescription());
        assertEquals(event.getEventDate(), updated.getEventDate());
        assertEquals(event.getLocation(), updated.getLocation());
        assertEquals(event.getCapacity(), updated.getCapacity());
    }

    @Test
    void testEventDetailsConsistency() {
        Event event = new Event();
        Map<String, Object> details = new HashMap<>();
        details.put("capacity", 200);
        details.put("isPublic", true);
        details.put("customField", "value");
        event.setDetails(details);

        // Verify details map is properly set
        assertEquals(200, event.getCapacity());
        assertTrue(event.isPublic());
        assertEquals("value", event.getDetails().get("customField"));

        // Test that updating values directly updates the details map
        event.setCapacity(300);
        event.setPublic(false);

        assertEquals(300, event.getDetails().get("capacity"));
        assertEquals(false, event.getDetails().get("isPublic"));
        // Custom field should remain unchanged
        assertEquals("value", event.getDetails().get("customField"));
    }

    @Test
    void testCreateEventWithNegativeCapacity() {
        // Create a local command to debug the issue
        CreateEventCommand command = new CreateEventCommand(
                mockEventRepository,
                mockEventSubject,
                "Negative Capacity Event",
                "Description",
                "2024-12-31",
                "Jakarta",
                1,
                -10,
                true);

        // Manually execute the command
        command.execute();

        // Check if the created event is not null
        Event event = command.getEvent();
        assertNotNull(event);
        assertEquals(-10, event.getCapacity());
        // This test verifies that the system accepts negative capacity (which might need validation)
    }

    @Test
    void testInitialDetailsMapCreation() {
        Event event = new Event();
        // Initially the details map should be null
        assertNull(event.getDetails());

        // Setting capacity should initialize the details map
        event.setCapacity(100);
        assertNotNull(event.getDetails());
        assertEquals(100, event.getCapacity());

        // Reset the event and initialize via setPublic
        event = new Event();
        assertNull(event.getDetails());
        event.setPublic(true);
        assertNotNull(event.getDetails());
        assertTrue(event.isPublic());
    }

    @Test
    void testCapacityDefaultValue() {
        Event event = new Event();
        // If capacity is not set, getCapacity should return null or a default value
        assertNull(event.getCapacity());
    }

    @Test
    void testEventWithMaxIntegerCapacity() {
        Event event = eventManager.createEvent(
                "Max Capacity Event", "Description", "2024-12-31", "Jakarta", 1, Integer.MAX_VALUE, true);
        assertNotNull(event);
        assertEquals(Integer.MAX_VALUE, event.getCapacity());
    }

    @Test
    void testNoArgsConstructor_Timestamps() {
        Event event = new Event();
        assertNull(event.getCreatedAt());
        assertNull(event.getUpdatedAt());
    }

    @Test
    void testGetCapacity_DetailsNotNull_CapacityMissing() {
        Event event = new Event();
        event.setDetails(new HashMap<>());
        assertNull(event.getCapacity());
    }

    @Test
    void testIsPublic_DetailsNotNull_IsPublicMissing() {
        Event event = new Event();
        event.setDetails(new HashMap<>());
        assertFalse(event.isPublic());
    }

    // Tests for Event(Integer id, Map<String, Object> details) constructor

    @Test
    void testEventConstructor_NullDetails() {
        Event event = new Event(1, null);
        assertEquals(Integer.valueOf(1), event.getId());
        assertNull(event.getDetails());
        // Fields are not initialized by the constructor if details is null
        assertNull(event.getTitle());
        assertNull(event.getDescription());
        assertNull(event.getEventDate());
        assertNull(event.getLocation());
        assertNull(event.getOrganizerId());
        assertNotNull(event.getCreatedAt()); // createdAt is always set
        assertNotNull(event.getUpdatedAt()); // updatedAt is always set
    }

    @Test
    void testEventConstructor_EmptyDetails() {
        Map<String, Object> details = new HashMap<>();
        Event event = new Event(1, details);

        assertEquals(Integer.valueOf(1), event.getId());
        assertSame(details, event.getDetails()); // The provided map instance is used
        assertEquals("", event.getTitle()); // Defaults to empty string
        assertEquals("", event.getDescription()); // Defaults to empty string
        assertEquals("", event.getEventDate()); // Defaults to empty string for "date" key
        assertEquals("", event.getLocation()); // Defaults to empty string
        assertNull(event.getOrganizerId()); // organizerId not in details, so remains null
        assertNotNull(event.getCreatedAt());
        assertNotNull(event.getUpdatedAt());
    }

    @Test
    void testEventConstructor_WithAllDetailsAndOrganizerIdAsInteger() {
        Map<String, Object> details = new HashMap<>();
        details.put("title", "Full Event Title");
        details.put("description", "Full Event Description");
        details.put("date", "2025-02-15");
        details.put("location", "Full Event Location");
        details.put("organizerId", 789); // Integer type

        Event event = new Event(10, details);

        assertEquals(Integer.valueOf(10), event.getId());
        assertEquals("Full Event Title", event.getTitle());
        assertEquals("Full Event Description", event.getDescription());
        assertEquals("2025-02-15", event.getEventDate());
        assertEquals("Full Event Location", event.getLocation());
        assertEquals(Integer.valueOf(789), event.getOrganizerId());
        assertNotNull(event.getCreatedAt());
        assertNotNull(event.getUpdatedAt());
    }

    @Test
    void testEventConstructor_WithOrganizerIdAsString() {
        Map<String, Object> details = new HashMap<>();
        details.put("organizerId", "101112"); // String type

        Event event = new Event(2, details);
        assertEquals(Integer.valueOf(101112), event.getOrganizerId()); // Should be parsed to Integer
    }

    @Test
    void testEventConstructor_WithOrganizerIdAsNonIntegerNonString() {
        Map<String, Object> details = new HashMap<>();
        details.put("organizerId", 123.45); // Double, neither Integer nor String

        Event event = new Event(3, details);
        // Based on current logic, organizerId should not be set from a Double.
        // It will remain null as it's not initialized by other means in this constructor path.
        assertNull(event.getOrganizerId());
    }

    @Test
    void testEventConstructor_OrganizerIdNotPresentInDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("title", "Event Lacking OrganizerId");
        // organizerId key is absent from the details map

        Event event = new Event(4, details);
        assertNull(event.getOrganizerId()); // Should be null
    }

    @Test
    void testSetCreatedAt() {
        Event event = new Event();
        Instant now = Instant.now();
        event.setCreatedAt(now);
        assertEquals(now, event.getCreatedAt());
    }

    @Test
    void testSetUpdatedAt() {
        Event event = new Event();
        Instant now = Instant.now();
        event.setUpdatedAt(now);
        assertEquals(now, event.getUpdatedAt());
    }

    @Test
    void testGetCapacity_DetailsContainsNullForCapacityKey() {
        Event event = new Event();
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("capacity", null);
        event.setDetails(detailsMap);
        assertNull(event.getCapacity());
    }

    @Test
    void testIsPublic_DetailsContainsNullForIsPublicKey_ShouldThrowNPE() {
        Event event = new Event();
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("isPublic", null);
        event.setDetails(detailsMap);
        assertThrows(NullPointerException.class, event::isPublic);
    }

    @Test
    void testSetId() {
        Event event = new Event();
        Integer id = 123;
        event.setId(id);
        assertEquals(id, event.getId());
    }

    @Test
    void testSetTitle() {
        Event event = new Event();
        String title = "New Title";
        event.setTitle(title);
        assertEquals(title, event.getTitle());
    }

    @Test
    void testSetDescription() {
        Event event = new Event();
        String description = "New Description";
        event.setDescription(description);
        assertEquals(description, event.getDescription());
    }

    @Test
    void testSetEventDate() {
        Event event = new Event();
        String eventDate = "2025-12-12";
        event.setEventDate(eventDate);
        assertEquals(eventDate, event.getEventDate());
    }

    @Test
    void testSetLocation() {
        Event event = new Event();
        String location = "New Location";
        event.setLocation(location);
        assertEquals(location, event.getLocation());
    }

    @Test
    void testSetOrganizerId() {
        Event event = new Event();
        Integer organizerId = 999;
        event.setOrganizerId(organizerId);
        assertEquals(organizerId, event.getOrganizerId());
    }

    @Test
    void testEventConstructor_WithInvalidOrganizerIdString() {
        Map<String, Object> details = new HashMap<>();
        details.put("organizerId", "not-an-integer");
        assertThrows(NumberFormatException.class, () -> {
            new Event(5, details);
        });
    }

    @Test
    void testGetSetDetails() {
        Event event = new Event();
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("key", "value");
        event.setDetails(newDetails);
        assertSame(newDetails, event.getDetails()); // Check if it's the same map instance
        assertEquals("value", event.getDetails().get("key"));

        // Test setting details to null
        event.setDetails(null);
        assertNull(event.getDetails());
    }

    @Test
    void testEventConstructor_WithNullValuesInDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("title", null);
        details.put("description", null);
        details.put("date", null);
        details.put("location", null);

        Event event = new Event(20, details);

        assertEquals(Integer.valueOf(20), event.getId());
        assertNull(event.getTitle()); // Verifies field is null, not default empty string
        assertNull(event.getDescription()); // Verifies field is null, not default empty string
        assertNull(event.getEventDate()); // Verifies field is null, not default empty string
        assertNull(event.getLocation()); // Verifies field is null, not default empty string
        assertNull(event.getOrganizerId()); // organizerId key is not in details
        assertNotNull(event.getCreatedAt());
        assertNotNull(event.getUpdatedAt());
    }
}
