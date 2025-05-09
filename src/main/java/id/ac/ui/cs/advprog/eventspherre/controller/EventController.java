package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.command.UpdateEventCommand;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.observer.UserObserver;
import id.ac.ui.cs.advprog.eventspherre.service.EventManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventManager eventManager;

    @PostMapping
    public Event createEvent(@RequestBody Map<String, Object> eventDetails) {
        return eventManager.createEvent(eventDetails);
    }
    

    @PutMapping("/{id}")
    public Event updateEvent(@PathVariable int id, @RequestBody Map<String, Object> updatedDetails) {
        Event event = eventManager.getEvent(id);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }
        UpdateEventCommand command = new UpdateEventCommand(event, updatedDetails);
        eventManager.executeCommand(command);
        return event;
    }

    

    @PostMapping("/{id}/observers")
    public String addObserver(@PathVariable int id, @RequestBody String username) {
        Event event = eventManager.getEvent(id);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }
        UserObserver observer = new UserObserver(username);
        eventManager.addUserToEvent(event, observer);
        return "Observer added successfully";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable("id") int id) {
    Event event = eventManager.getEvent(id);
    if (event == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(event);
}

    @GetMapping
    public ResponseEntity<Map<Integer, Event>> getAllEvents() {
        return ResponseEntity.ok(eventManager.getAllEvents());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable("id") int id) {
        eventManager.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully");
    }

}

