package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(found).isPresent();
        found.ifPresent(e -> {
            assertThat(e.getTitle()).isEqualTo("Test Event");
            assertThat(e.getDescription()).isEqualTo("Test Description");
            assertThat(e.getEventDate()).isEqualTo("2024-12-31");
            assertThat(e.getLocation()).isEqualTo("Jakarta");
            assertThat(e.getOrganizerId()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Find all Events")
    void testFindAllEvents() {
        Event event1 = createTestEvent("Event 1", "Description 1", "2024-12-01", "Jakarta", 1);
        Event event2 = createTestEvent("Event 2", "Description 2", "2024-12-15", "Bandung", 1);
        Event event3 = createTestEvent("Event 3", "Description 3", "2024-12-31", "Surabaya", 2);

        eventRepository.saveAll(Arrays.asList(event1, event2, event3));

        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(3);

        // Verify events are returned (order may vary)
        assertThat(events.stream().map(Event::getTitle).toList())
            .containsExactlyInAnyOrder("Event 1", "Event 2", "Event 3");
    }

    @Test
    @DisplayName("Update Event properties")
    void testUpdateEvent() {
        Event event = createTestEvent("Original Title", "Original Description", "2024-12-31", "Jakarta", 1);
        Event saved = eventRepository.save(event);
        Integer eventId = saved.getId();

        // Fetch the entity to update (or use the 'saved' instance directly if preferred)
        Event eventToUpdate = eventRepository.findById(eventId)
            .orElseThrow(() -> new AssertionError("Event not found for update"));

        eventToUpdate.setTitle("Updated Title");
        eventToUpdate.setDescription("Updated Description");
        eventToUpdate.setEventDate("2025-01-15");
        eventToUpdate.setLocation("Bali");
        eventToUpdate.setOrganizerId(2);

        eventRepository.save(eventToUpdate);

        Optional<Event> updated = eventRepository.findById(eventId);
        assertThat(updated).isPresent();
        updated.ifPresent(e -> {
            assertThat(e.getTitle()).isEqualTo("Updated Title");
            assertThat(e.getDescription()).isEqualTo("Updated Description");
            assertThat(e.getEventDate()).isEqualTo("2025-01-15");
            assertThat(e.getLocation()).isEqualTo("Bali");
            assertThat(e.getOrganizerId()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Delete Event by ID")
    void testDeleteEventById() {
        Event event = createTestEvent("Event to delete", "Will be deleted", "2024-12-31", "Jakarta", 1);
        Event saved = eventRepository.save(event);
        Integer eventId = saved.getId();
        
        assertThat(eventRepository.existsById(eventId)).isTrue();

        eventRepository.deleteById(eventId);
        
        assertThat(eventRepository.existsById(eventId)).isFalse();
        assertThat(eventRepository.findById(eventId)).isNotPresent();
    }

    @Test
    @DisplayName("Count Events")
    void testCountEvents() {
        assertThat(eventRepository.count()).isEqualTo(0L);

        Event event1 = createTestEvent("Event 1", "Description 1", "2024-12-01", "Jakarta", 1);
        Event event2 = createTestEvent("Event 2", "Description 2", "2024-12-15", "Bandung", 1);
        
        eventRepository.saveAll(Arrays.asList(event1, event2));
        
        assertThat(eventRepository.count()).isEqualTo(2L);

        // Ensure event1 has an ID after saveAll
        assertThat(event1.getId()).isNotNull();
        eventRepository.deleteById(event1.getId());
        
        assertThat(eventRepository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Exists by ID check")
    void testExistsById() {
        Event event = createTestEvent("Test Event", "Test Description", "2024-12-31", "Jakarta", 1);
        Event saved = eventRepository.save(event);

        assertThat(eventRepository.existsById(saved.getId())).isTrue();
    }
}

