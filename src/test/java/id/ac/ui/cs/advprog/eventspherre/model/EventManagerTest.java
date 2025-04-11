package id.ac.ui.cs.advprog.eventspherre.model;

import id.ac.ui.cs.advprog.eventspherre.command.Command;
import id.ac.ui.cs.advprog.eventspherre.command.UpdateEventCommand;
import id.ac.ui.cs.advprog.eventspherre.observer.UserObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import id.ac.ui.cs.advprog.eventspherre.service.EventManager;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class EventManagerTest {
    private EventManager eventManager;
    private Map<String, Object> initialDetails;

    @BeforeEach
    void setUp() {
        eventManager = new EventManager();
        initialDetails = new HashMap<>();
        initialDetails.put("title", "Test Event");
        initialDetails.put("date", "2024-12-31");
    }

    @Test
    void testCreateEvent() {
        Event event = eventManager.createEvent(initialDetails);
        assertNotNull(event);
        assertEquals(1, event.getId());
        assertEquals(initialDetails, event.getDetails());
    }

    @Test
    void testUpdateEventCommand() {
        Event event = eventManager.createEvent(initialDetails);
        Map<String, Object> updatedDetails = new HashMap<>();
        updatedDetails.put("title", "Updated Event");
        updatedDetails.put("date", "2025-01-01");

        Command updateCommand = new UpdateEventCommand(event, updatedDetails);
        eventManager.executeCommand(updateCommand);

        assertEquals(updatedDetails, event.getDetails());
    }

    @Test
    void testObserverNotification() {
        Event event = eventManager.createEvent(initialDetails);
        UserObserver user = new UserObserver("TestUser");
        eventManager.addUserToEvent(event, user);

        Map<String, Object> updatedDetails = new HashMap<>();
        updatedDetails.put("title", "Updated Event");

        Command updateCommand = new UpdateEventCommand(event, updatedDetails);
        eventManager.executeCommand(updateCommand);

        // We'll verify user notification by checking output in the console.
        // In a real scenario, you might use Mockito or similar for better verification.
    }

    @Test
    void testGetEvent() {
        Event event = eventManager.createEvent(initialDetails);
        Event retrievedEvent = eventManager.getEvent(event.getId());
        assertEquals(event, retrievedEvent);
    }
}
