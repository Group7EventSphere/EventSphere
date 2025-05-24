package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.observer.EventSubject;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateEventCommandTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventSubject eventSubject;

    @Mock
    private Event originalEvent;

    @Mock
    private Event updatedEvent;

    private UpdateEventCommand updateEventCommand;

    private final Integer eventId = 1;
    private final String title = "Updated Event";
    private final String description = "Updated Description";
    private final String eventDate = "2025-07-01";
    private final String location = "Updated Location";
    private final Integer capacity = 200;
    private final Boolean isPublic = false;

    private final Integer organizerId = 456;
    private final Map<String, Object> originalDetails = new HashMap<>();

    @BeforeEach
    public void setUp() {
        // Setup original event details
        originalDetails.put("title", "Original Event");
        originalDetails.put("description", "Original Description");
        originalDetails.put("date", "2025-06-01");
        originalDetails.put("location", "Original Location");
        originalDetails.put("capacity", 100);
        originalDetails.put("isPublic", true);
        originalDetails.put("organizerId", organizerId);

        // Create the command
        updateEventCommand = new UpdateEventCommand(
            eventRepository,
            eventSubject,
            eventId,
            title,
            description,
            eventDate,
            location,
            capacity,
            isPublic
        );
    }

    @Test
    public void testExecute() {
        // Setup mocks for this test
        when(originalEvent.getDetails()).thenReturn(originalDetails);
        when(originalEvent.getOrganizerId()).thenReturn(organizerId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(originalEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        // Act
        updateEventCommand.execute();

        // Assert

        // Verify repository operations
        verify(eventRepository).findById(eventId);

        // Verify event properties were updated
        verify(originalEvent).setTitle(title);
        verify(originalEvent).setDescription(description);
        verify(originalEvent).setEventDate(eventDate);
        verify(originalEvent).setLocation(location);
        verify(originalEvent).setCapacity(capacity);
        verify(originalEvent).setPublic(isPublic);
        verify(originalEvent).setUpdatedAt(any(Instant.class));

        // Verify details map was updated
        ArgumentCaptor<Map<String, Object>> detailsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(originalEvent).setDetails(detailsCaptor.capture());
        Map<String, Object> capturedDetails = detailsCaptor.getValue();
        assertEquals(title, capturedDetails.get("title"));
        assertEquals(description, capturedDetails.get("description"));
        assertEquals(eventDate, capturedDetails.get("date"));
        assertEquals(location, capturedDetails.get("location"));
        assertEquals(capacity, capturedDetails.get("capacity"));
        assertEquals(isPublic, capturedDetails.get("isPublic"));
        assertEquals(organizerId, capturedDetails.get("organizerId"));

        // Verify repository save and notification
        verify(eventRepository).save(originalEvent);
        verify(eventSubject).notifyEventUpdated(updatedEvent);
    }

    @Test
    public void testExecuteEventNotFound() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        updateEventCommand.execute();

        // Assert
        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
        verify(eventSubject, never()).notifyEventUpdated(any(Event.class));
    }    @Test
    public void testUndo() {
        // Setup - Execute first
        when(originalEvent.getDetails()).thenReturn(new HashMap<>(originalDetails));
        when(originalEvent.getOrganizerId()).thenReturn(organizerId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(originalEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);
        
        // First execute the command
        updateEventCommand.execute();
        reset(eventRepository, eventSubject, originalEvent);

        // Setup for undo
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(originalEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(originalEvent);

        // Act
        updateEventCommand.undo();

        // Assert
        verify(eventRepository).findById(eventId);
        verify(originalEvent).setDetails(any(Map.class));
        verify(eventRepository).save(originalEvent);
        verify(eventSubject).notifyEventUpdated(originalEvent);
    }    @Test
    public void testUndoEventNotFound() {
        // Setup - Execute first
        when(originalEvent.getDetails()).thenReturn(new HashMap<>(originalDetails));
        when(originalEvent.getOrganizerId()).thenReturn(organizerId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(originalEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);
        
        // First execute the command
        updateEventCommand.execute();
        reset(eventRepository, eventSubject, originalEvent);

        // Setup for undo
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        updateEventCommand.undo();

        // Assert
        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
        verify(eventSubject, never()).notifyEventUpdated(any(Event.class));
    }    @Test
    public void testGetEvent() {
        // Setup mocks for execute
        when(originalEvent.getDetails()).thenReturn(originalDetails);
        when(originalEvent.getOrganizerId()).thenReturn(organizerId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(originalEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);
        
        // Arrange
        updateEventCommand.execute();

        // Act
        Event result = updateEventCommand.getEvent();

        // Assert
        assertEquals(updatedEvent, result);
    }
}
