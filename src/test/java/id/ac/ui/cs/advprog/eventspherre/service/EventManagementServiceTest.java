package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.command.CreateEventCommand;
import id.ac.ui.cs.advprog.eventspherre.command.EventCommandInvoker;
import id.ac.ui.cs.advprog.eventspherre.command.ToggleEventVisibilityCommand;
import id.ac.ui.cs.advprog.eventspherre.command.UpdateEventCommand;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.observer.EventObserver;
import id.ac.ui.cs.advprog.eventspherre.observer.EventSubject;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventManagementServiceTest {

    private EventManagementService eventManagementService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventSubject eventSubject;

    @Mock
    private EventCommandInvoker commandInvoker;

    @Mock
    private EventObserver eventObserver;

    @Mock
    private Event mockEvent;    @BeforeEach
    void setUp() {
        eventManagementService = new EventManagementService(
            eventRepository,
            eventSubject,
            commandInvoker
        );
    }    @Test
    void testRegisterObserver() {
        // Act
        eventManagementService.registerObserver(eventObserver);

        // Assert
        verify(eventSubject).addObserver(eventObserver);
    }    @Test
    void testUnregisterObserver() {
        // Act
        eventManagementService.unregisterObserver(eventObserver);

        // Assert
        verify(eventSubject).removeObserver(eventObserver);
    }    @Test
    void testCreateEvent() {
        // Arrange
        String title = "Test Event";
        String description = "Test Description";
        String eventDate = "2025-05-25";
        String location = "Test Location";
        Integer organizerId = 123;
        Integer capacity = 100;
        Boolean isPublic = true;        ArgumentCaptor<CreateEventCommand> commandCaptor = ArgumentCaptor.forClass(CreateEventCommand.class);

        // Mock command behavior
        doAnswer(invocation -> {
            CreateEventCommand command = invocation.getArgument(0);
            // Simulate setting the event by reflection
            try {
                java.lang.reflect.Field eventField = CreateEventCommand.class.getDeclaredField("createdEvent");
                eventField.setAccessible(true);
                eventField.set(command, mockEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).when(commandInvoker).executeCommand(any(CreateEventCommand.class));

        // Act
        Event result = eventManagementService.createEvent(title, description, eventDate, location, organizerId, capacity, isPublic);

        // Assert
        verify(commandInvoker).executeCommand(commandCaptor.capture());

        // Verify command properties through reflection
        assertEquals(mockEvent, result);
    }    @Test
    void testUpdateEvent() {
        // Arrange
        Integer eventId = 1;
        String title = "Updated Event";
        String description = "Updated Description";
        String eventDate = "2025-06-01";
        String location = "Updated Location";
        Integer capacity = 200;
        Boolean isPublic = false;

        ArgumentCaptor<UpdateEventCommand> commandCaptor = ArgumentCaptor.forClass(UpdateEventCommand.class);

        // Mock command behavior
        doAnswer(invocation -> {
            UpdateEventCommand command = invocation.getArgument(0);
            // Simulate setting the event by reflection
            try {
                java.lang.reflect.Field eventField = UpdateEventCommand.class.getDeclaredField("updatedEvent");
                eventField.setAccessible(true);
                eventField.set(command, mockEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).when(commandInvoker).executeCommand(any(UpdateEventCommand.class));

        // Act
        Event result = eventManagementService.updateEvent(eventId, title, description, eventDate, location, capacity, isPublic);

        // Assert
        verify(commandInvoker).executeCommand(commandCaptor.capture());
        assertEquals(mockEvent, result);
    }    @Test
    void testToggleEventVisibility() {
        // Arrange
        Integer eventId = 1;

        ArgumentCaptor<ToggleEventVisibilityCommand> commandCaptor = ArgumentCaptor.forClass(ToggleEventVisibilityCommand.class);

        // Mock command behavior
        doAnswer(invocation -> {
            ToggleEventVisibilityCommand command = invocation.getArgument(0);
            // Simulate setting the event by reflection
            try {
                java.lang.reflect.Field eventField = ToggleEventVisibilityCommand.class.getDeclaredField("event");
                eventField.setAccessible(true);
                eventField.set(command, mockEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).when(commandInvoker).executeCommand(any(ToggleEventVisibilityCommand.class));

        // Act
        Event result = eventManagementService.toggleEventVisibility(eventId);

        // Assert
        verify(commandInvoker).executeCommand(commandCaptor.capture());
        assertEquals(mockEvent, result);
    }    @Test
    void testUndoLastOperation_Success() {
        // Arrange
        when(commandInvoker.hasCommandHistory()).thenReturn(true);

        // Act
        boolean result = eventManagementService.undoLastOperation();

        // Assert
        assertTrue(result);
        verify(commandInvoker).undoLastCommand();
    }    @Test
    void testUndoLastOperation_NoHistory() {
        // Arrange
        when(commandInvoker.hasCommandHistory()).thenReturn(false);

        // Act
        boolean result = eventManagementService.undoLastOperation();

        // Assert
        assertFalse(result);
        verify(commandInvoker, never()).undoLastCommand();
    }    @Test
    void testGetEvent() {
        // Arrange
        Integer eventId = 1;
        when(eventRepository.findById(eventId)).thenReturn(java.util.Optional.of(mockEvent));

        // Act
        Event result = eventManagementService.getEvent(eventId);

        // Assert
        assertEquals(mockEvent, result);
    }    @Test
    void testGetAllEvents() {
        // Arrange
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventRepository.findAll()).thenReturn(events);

        // Act
        List<Event> result = eventManagementService.getAllEvents();

        // Assert
        assertEquals(events, result);
    }    @Test
    void testCreateEventAsync() throws Exception {
        // Arrange
        String title = "Async Event";
        String description = "Async Description";
        String eventDate = "2025-07-01";
        String location = "Async Location";
        Integer organizerId = 456;
        Integer capacity = 300;
        Boolean isPublic = true;

        // Spy on the service to mock synchronous createEvent method
        EventManagementService spyService = spy(eventManagementService);
        doReturn(mockEvent).when(spyService).createEvent(
            title, description, eventDate, location, organizerId, capacity, isPublic
        );

        // Act
        CompletableFuture<Event> futureResult = spyService.createEventAsync(
            title, description, eventDate, location, organizerId, capacity, isPublic
        );

        // Assert
        Event result = futureResult.get();  // Will block until complete
        assertEquals(mockEvent, result);
    }    @Test
    void testUpdateEventAsync() throws Exception {
        // Arrange
        Integer eventId = 1;
        String title = "Async Updated Event";
        String description = "Async Updated Description";
        String eventDate = "2025-07-01";
        String location = "Async Location";
        Integer capacity = 200;
        Boolean isPublic = false;

        // Spy on the service to mock synchronous updateEvent method
        EventManagementService spyService = spy(eventManagementService);
        doReturn(mockEvent).when(spyService).updateEvent(
            eventId, title, description, eventDate, location, capacity, isPublic
        );

        // Act
        CompletableFuture<Event> futureResult = spyService.updateEventAsync(
            eventId, title, description, eventDate, location, capacity, isPublic
        );

        // Assert
        Event result = futureResult.get();  // Will block until complete
        assertEquals(mockEvent, result);
    }    @Test
    void testFindEventsByOrganizerId() {
        // Arrange
        Integer organizerId = 123;
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventRepository.findByOrganizerId(organizerId)).thenReturn(events);

        // Act
        List<Event> result = eventManagementService.findEventsByOrganizerId(organizerId);

        // Assert
        assertEquals(events, result);
    }    @Test
    void testFindPublicEvents() {
        // Arrange
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventRepository.findByIsPublicTrue()).thenReturn(events);

        // Act
        List<Event> result = eventManagementService.findPublicEvents();

        // Assert
        assertEquals(events, result);
    }    @Test
    void testGetEventById() {
        // Arrange
        Integer eventId = 1;
        when(eventRepository.findById(eventId)).thenReturn(java.util.Optional.of(mockEvent));

        // Act
        Event result = eventManagementService.getEventById(eventId);

        // Assert
        assertEquals(mockEvent, result);
        verify(eventRepository).findById(eventId);
    }
}
