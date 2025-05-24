package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventManagementServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventManagementService eventManagementService;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1);
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setEventDate(Instant.now().toString()); // Changed to String
        event.setLocation("Test Location");
        event.setOrganizerId(101);
        event.setCapacity(100);
        event.setPublic(true);
    }

    @Test
    void testCreateEvent() {
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        Event createdEvent = eventManagementService.createEvent(
                event.getTitle(),
                event.getDescription(),
                event.getEventDate(),
                event.getLocation(),
                event.getOrganizerId(),
                event.getCapacity(),
                event.isPublic()
        );
        assertNotNull(createdEvent);
        assertEquals(event.getTitle(), createdEvent.getTitle());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCreateEventAsync() throws ExecutionException, InterruptedException {
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        CompletableFuture<Event> futureEvent = eventManagementService.createEventAsync(
                event.getTitle(),
                event.getDescription(),
                event.getEventDate(),
                event.getLocation(),
                event.getOrganizerId(),
                event.getCapacity(),
                event.isPublic()
        );
        Event createdEvent = futureEvent.get();
        assertNotNull(createdEvent);
        assertEquals(event.getTitle(), createdEvent.getTitle());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testGetEvent() {
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        Event foundEvent = eventManagementService.getEvent(1);
        assertNotNull(foundEvent);
        assertEquals(event.getTitle(), foundEvent.getTitle());
        verify(eventRepository, times(1)).findById(1);
    }

    @Test
    void testGetEvent_NotFound() {
        when(eventRepository.findById(99)).thenReturn(Optional.empty());
        Event result = eventManagementService.getEvent(99);
        assertNull(result);
        verify(eventRepository, times(1)).findById(99);
    }

    @Test
    void testGetAllEvents() {
        List<Event> events = new ArrayList<>();
        events.add(event);
        when(eventRepository.findAll()).thenReturn(events);

        List<Event> result = eventManagementService.getAllEvents();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testUpdateEvent() {
        String newTitle = "Updated Event";
        String newDescription = "Updated Description";
        String newDate = Instant.now().toString();
        String newLocation = "Updated Location";
        int newCapacity = 200;
        boolean newIsPublic = false;

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event savedEvent = invocation.getArgument(0);
            assertEquals(newTitle, savedEvent.getTitle());
            assertEquals(newDescription, savedEvent.getDescription());
            assertEquals(newDate, savedEvent.getEventDate());
            assertEquals(newLocation, savedEvent.getLocation());
            assertEquals(newCapacity, savedEvent.getCapacity());
            assertEquals(newIsPublic, savedEvent.isPublic());
            return savedEvent;
        });

        Event updatedEvent = eventManagementService.updateEvent(
                1, newTitle, newDescription, newDate, newLocation, newCapacity, newIsPublic
        );

        assertNotNull(updatedEvent);
        assertEquals(newTitle, updatedEvent.getTitle());
        verify(eventRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testUpdateEvent_NotFound() {
        when(eventRepository.findById(99)).thenReturn(Optional.empty());
        Event result = eventManagementService.updateEvent(
                99, "Any Title", "Any Description", "Any Date",
                "Any Location", 100, true
        );
        assertNull(result);
        verify(eventRepository, times(1)).findById(99);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testUpdateEventAsync() throws ExecutionException, InterruptedException {
        String newTitle = "Updated Event";
        String newDescription = "Updated Description";
        String newDate = Instant.now().toString();
        String newLocation = "Updated Location";
        int newCapacity = 200;
        boolean newIsPublic = false;

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<Event> futureEvent = eventManagementService.updateEventAsync(
                1, newTitle, newDescription, newDate, newLocation, newCapacity, newIsPublic
        );

        Event updatedEvent = futureEvent.get();
        assertNotNull(updatedEvent);
        assertEquals(newTitle, updatedEvent.getTitle());
        verify(eventRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testDeleteEvent() {
        doNothing().when(eventRepository).deleteById(1);
        eventManagementService.deleteEvent(1);
        verify(eventRepository, times(1)).deleteById(1);
    }

    @Test
    void testClearAllEvents() {
        doNothing().when(eventRepository).deleteAll();
        eventManagementService.clearAllEvents();
        verify(eventRepository, times(1)).deleteAll();
    }

    @Test
    void testGetEventById() {
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        Event foundEvent = eventManagementService.getEventById(1);
        assertNotNull(foundEvent);
        assertEquals(event.getTitle(), foundEvent.getTitle());
        verify(eventRepository, times(1)).findById(1);
    }

    @Test
    void testGetEventById_NotFound() {
        when(eventRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> eventManagementService.getEventById(1));
        verify(eventRepository, times(1)).findById(1);
    }
}
