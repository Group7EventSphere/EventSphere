package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.sql.init.mode=never")
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    private Event createTestEvent(String title, String description, String eventDate, String location, Integer organizerId) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setEventDate(eventDate);
        event.setLocation(location);
        event.setOrganizerId(organizerId);
        return event;
    }

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
    }

    @Test
    @DisplayName("Save and find Event by ID")
    void testSaveAndFindEvent() {
        Event event = createTestEvent("Test Event", "Test Description", "2024-12-31", "Jakarta", 1);

        Event saved = eventRepository.save(event);
        Optional<Event> found = eventRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Test Event", found.get().getTitle());
        assertEquals("Test Description", found.get().getDescription());
        assertEquals("2024-12-31", found.get().getEventDate());
        assertEquals("Jakarta", found.get().getLocation());
        assertEquals(1, found.get().getOrganizerId());
    }

    @Test
    @DisplayName("Find all Events")
    void testFindAllEvents() {
        Event event1 = createTestEvent("Event 1", "Description 1", "2024-12-01", "Jakarta", 1);
        Event event2 = createTestEvent("Event 2", "Description 2", "2024-12-15", "Bandung", 1);
        Event event3 = createTestEvent("Event 3", "Description 3", "2024-12-31", "Surabaya", 2);

        eventRepository.saveAll(Arrays.asList(event1, event2, event3));

        List<Event> events = eventRepository.findAll();
        assertEquals(3, events.size());
        
        // Verify events are returned (order may vary)
        List<String> eventTitles = events.stream().map(Event::getTitle).toList();
        assertTrue(eventTitles.containsAll(List.of("Event 1", "Event 2", "Event 3")));
    }

    @Test
    @DisplayName("Update Event properties")
    void testUpdateEvent() {
        Event event = createTestEvent("Original Title", "Original Description", "2024-12-31", "Jakarta", 1);
        Event saved = eventRepository.save(event);
        UUID eventId = saved.getId();

        // Update event properties
        saved.setTitle("Updated Title");
        saved.setDescription("Updated Description");
        saved.setEventDate("2025-01-15");
        saved.setLocation("Bali");
        saved.setOrganizerId(2);
        
        eventRepository.save(saved);
        
        Optional<Event> updated = eventRepository.findById(eventId);
        assertTrue(updated.isPresent());
        assertEquals("Updated Title", updated.get().getTitle());
        assertEquals("Updated Description", updated.get().getDescription());
        assertEquals("2025-01-15", updated.get().getEventDate());
        assertEquals("Bali", updated.get().getLocation());
        assertEquals(2, updated.get().getOrganizerId());
    }

    @Test
    @DisplayName("Delete Event by ID")
    void testDeleteEventById() {
        Event event = createTestEvent("Event to delete", "Will be deleted", "2024-12-31", "Jakarta", 1);
        Event saved = eventRepository.save(event);
        UUID eventId = saved.getId();
        
        // Verify event exists
        assertTrue(eventRepository.findById(eventId).isPresent());
        
        // Delete the event
        eventRepository.deleteById(eventId);
        
        // Verify event no longer exists
        assertFalse(eventRepository.findById(eventId).isPresent());
    }

    @Test
    @DisplayName("Count Events")
    void testCountEvents() {
        assertEquals(0, eventRepository.count());
        
        Event event1 = createTestEvent("Event 1", "Description 1", "2024-12-01", "Jakarta", 1);
        Event event2 = createTestEvent("Event 2", "Description 2", "2024-12-15", "Bandung", 1);
        
        eventRepository.saveAll(Arrays.asList(event1, event2));
        
        assertEquals(2, eventRepository.count());
        
        eventRepository.deleteById(event1.getId());
        
        assertEquals(1, eventRepository.count());
    }

    @Test
    @DisplayName("Exists by ID check")
    void testExistsById() {
        Event event = createTestEvent("Test Event", "Test Description", "2024-12-31", "Jakarta", 1);
        Event saved = eventRepository.save(event);
        
        assertTrue(eventRepository.existsById(saved.getId()));
        assertFalse(eventRepository.existsById(UUID.randomUUID()));
    }
}