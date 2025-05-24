package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.observer.EventSubject;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ToggleEventVisibilityCommandTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventSubject eventSubject;

    @Mock
    private Event event;

    private ToggleEventVisibilityCommand toggleCommand;
    private final Integer eventId = 1;
    private final Map<String, Object> eventDetails = new HashMap<>();

    @BeforeEach
    public void setUp() {
        // Setup event details map
        eventDetails.put("title", "Test Event");
        eventDetails.put("isPublic", true);

        // Create command
        toggleCommand = new ToggleEventVisibilityCommand(
            eventRepository,
            eventSubject,
            eventId
        );
    }

    @Test
    public void testExecuteTogglePublicToPrivate() {
        // Setup
        when(event.isPublic()).thenReturn(true);
        when(event.getDetails()).thenReturn(eventDetails);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Act
        toggleCommand.execute();

        // Assert
        verify(event).setPublic(false);
        verify(event).setUpdatedAt(any(Instant.class));
        verify(eventRepository).save(event);
        verify(eventSubject).notifyEventVisibilityChanged(event, false);
        assertEquals(false, eventDetails.get("isPublic"));
    }

    @Test
    public void testExecuteTogglePrivateToPublic() {
        // Setup
        when(event.isPublic()).thenReturn(false);
        eventDetails.put("isPublic", false);
        when(event.getDetails()).thenReturn(eventDetails);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Act
        toggleCommand.execute();

        // Assert
        verify(event).setPublic(true);
        verify(event).setUpdatedAt(any(Instant.class));
        verify(eventRepository).save(event);
        verify(eventSubject).notifyEventVisibilityChanged(event, true);
        assertEquals(true, eventDetails.get("isPublic"));
    }

    @Test
    public void testExecuteEventNotFound() {
        // Setup
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        toggleCommand.execute();

        // Assert
        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
        verify(eventSubject, never()).notifyEventVisibilityChanged(any(Event.class), anyBoolean());
    }    @Test
    public void testUndo() {
        // Setup - Execute first to set the previous state
        when(event.isPublic()).thenReturn(true);
        when(event.getDetails()).thenReturn(eventDetails);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        
        toggleCommand.execute();

        // Store the previous visibility state before resetting
        boolean previousVisibilityState = true;

        // Reset mocks without resetting the toggleCommand
        reset(eventSubject, eventRepository, event);

        // Setup for undo - don't reset eventRepository completely
        // but set up specific behavior
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(event.getDetails()).thenReturn(eventDetails);

        // Act
        toggleCommand.undo();

        // Assert
        verify(event).setPublic(previousVisibilityState);
        verify(eventRepository).save(event);  // Now this will only verify save calls after the reset
        verify(eventSubject).notifyEventVisibilityChanged(event, previousVisibilityState);
        assertEquals(previousVisibilityState, eventDetails.get("isPublic"));
    }    @Test
    public void testUndoEventNotFound() {
        // Setup - Execute first
        when(event.isPublic()).thenReturn(true);
        when(event.getDetails()).thenReturn(eventDetails);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        
        toggleCommand.execute();

        // Reset mocks
        reset(eventRepository, eventSubject);

        // Setup undo
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        toggleCommand.undo();

        // Assert
        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
        verify(eventSubject, never()).notifyEventVisibilityChanged(any(), anyBoolean());
    }

    @Test
    public void testGetEvent() {
        // Arrange
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(event.getDetails()).thenReturn(eventDetails);

        // Act
        toggleCommand.execute(); // This should set the event field in the command
        Event result = toggleCommand.getEvent();

        // Assert
        assertEquals(event, result);
    }
}
