package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventManagementService eventManagementService;

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Map<String, Object> eventDetails) {
        String title = (String) eventDetails.get("title");
        String description = (String) eventDetails.get("description");
        String eventDate = (String) eventDetails.get("eventDate");
        String location = (String) eventDetails.get("location");
        Integer organizerId = (Integer) eventDetails.get("organizerId");

        Event event = eventManagementService.createEvent(
                title, description, eventDate, location, organizerId);

        return ResponseEntity.ok(event);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable("id") UUID id) {
        Event event = eventManagementService.getEvent(id);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventManagementService.getAllEvents());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable UUID id, @RequestBody Map<String, Object> eventDetails) {
        String title = (String) eventDetails.get("title");
        String description = (String) eventDetails.get("description");
        String eventDate = (String) eventDetails.get("eventDate");
        String location = (String) eventDetails.get("location");
        Integer organizerId = (Integer) eventDetails.get("organizerId");

        Event updatedEvent = eventManagementService.updateEvent(
                id, title, description, eventDate, location, organizerId);

        if (updatedEvent == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable("id") UUID id) {
        Event event = eventManagementService.getEvent(id);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }

        eventManagementService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully");
    }
}