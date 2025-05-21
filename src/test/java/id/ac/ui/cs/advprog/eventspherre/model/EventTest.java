package id.ac.ui.cs.advprog.eventspherre.model;

import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {
    private EventManagementService eventManager;

    @BeforeEach
    void setUp() {
        eventManager = new EventManagementService();
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
        assertEquals(event, retrievedEvent);
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