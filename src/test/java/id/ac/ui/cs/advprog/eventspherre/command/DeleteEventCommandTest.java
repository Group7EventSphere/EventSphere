package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.observer.EventSubject;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteEventCommandTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventSubject eventSubject;

    @Mock
    private Event event;

    private DeleteEventCommand deleteCommand;
    private final Integer eventId = 1;

    @BeforeEach
    void setUp() {
        deleteCommand = new DeleteEventCommand(
            eventRepository,
            eventSubject,
            eventId
        );
    }

    @Test
    void testExecute() {
        // Setup
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        deleteCommand.execute();

        // Assert
        verify(eventRepository).findById(eventId);
        verify(eventRepository).delete(event);
        verify(eventSubject).notifyEventDeleted(event);
    }

    @Test
    void testExecuteEventNotFound() {
        // Setup
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        deleteCommand.execute();

        // Assert
        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).delete(any(Event.class));
        verify(eventSubject, never()).notifyEventDeleted(any(Event.class));
    }

    @Test
    void testUndo() {
        // Setup - Execute first to set the deletedEvent
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        
        deleteCommand.execute();

        // Act
        deleteCommand.undo();

        // Assert
        verify(eventRepository).save(event);
        verify(eventSubject).notifyEventCreated(event);
    }

    @Test
    void testUndoWithoutExecute() {
        // Act
        deleteCommand.undo();

        // Assert - verify nothing happens when undo is called without execute
        verify(eventRepository, never()).save(any(Event.class));
        verify(eventSubject, never()).notifyEventCreated(any(Event.class));
    }

    @Test
    void testGetDeletedEvent() {
        // Setup - Execute first to set the deletedEvent
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        
        deleteCommand.execute();

        // Act
        Event result = deleteCommand.getDeletedEvent();

        // Assert
        assertEquals(event, result);
    }

    @Test
    void testGetDeletedEventWithoutExecute() {
        // Act
        Event result = deleteCommand.getDeletedEvent();

        // Assert
        assertNull(result);
    }

    @Test
    void testUndoResetsDeletedEventToNull() {
        // Setup - Execute first to set the deletedEvent
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        
        deleteCommand.execute();
        
        // Verify deletedEvent is set
        assertNotNull(deleteCommand.getDeletedEvent());

        // Act
        deleteCommand.undo();

        // Assert - deletedEvent should be null after undo
        assertNull(deleteCommand.getDeletedEvent());
    }
}
