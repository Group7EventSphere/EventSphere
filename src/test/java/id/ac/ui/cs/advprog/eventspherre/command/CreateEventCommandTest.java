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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateEventCommandTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventSubject eventSubject;

    @Mock
    private Event savedEvent;

    private CreateEventCommand createEventCommand;

    private final String title = "Test Event";
    private final String description = "Test Event Description";
    private final String eventDate = "2025-06-01";
    private final String location = "Test Location";
    private final Integer organizerId = 123;
    private final Integer capacity = 100;
    private final Boolean isPublic = true;

    @BeforeEach
    void setUp() {
        createEventCommand = new CreateEventCommand(
            eventRepository,
            eventSubject,
            title,
            description,
            eventDate,
            location,
            organizerId,
            capacity,
            isPublic
        );    }

    @Test
    void testExecute() {
        // Setup the mock behavior that's needed for this test
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // Act
        createEventCommand.execute();

        // Assert

        // Capture the Event being saved
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        // Verify event properties
        Event capturedEvent = eventCaptor.getValue();
        assertEquals(title, capturedEvent.getTitle());
        assertEquals(description, capturedEvent.getDescription());
        assertEquals(eventDate, capturedEvent.getEventDate());
        assertEquals(location, capturedEvent.getLocation());
        assertEquals(organizerId, capturedEvent.getOrganizerId());
        assertEquals(capacity, capturedEvent.getCapacity());
        assertEquals(isPublic, capturedEvent.isPublic());

        // Verify notification
        verify(eventSubject).notifyEventCreated(savedEvent);
    }

    @Test
    void testUndo() {
        // Arrange
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        createEventCommand.execute();

        // Act
        createEventCommand.undo();

        // Assert
        verify(eventRepository).delete(savedEvent);
        verify(eventSubject).notifyEventDeleted(savedEvent);    }

    @Test
    void testUndoWithoutExecute() {
        // Act
        createEventCommand.undo();

        // Assert - verify nothing happens
        verify(eventRepository, never()).delete(any());
        verify(eventSubject, never()).notifyEventDeleted(any());
    }

    @Test
    void testGetEvent() {
        // Arrange
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        createEventCommand.execute();

        // Act
        Event result = createEventCommand.getEvent();

        // Assert
        assertEquals(savedEvent, result);
    }
}
