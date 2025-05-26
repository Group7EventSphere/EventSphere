package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import id.ac.ui.cs.advprog.eventspherre.observer.EventSubject;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class ToggleEventVisibilityCommand implements EventCommand {
    private final EventRepository eventRepository;
    private final EventSubject eventSubject;
    private final Integer eventId;

    private Event event;
    private boolean previousVisibilityState;

    @Override
    public void execute() {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            return;
        }

        event = optionalEvent.get();
        previousVisibilityState = event.isPublic();

        // Toggle visibility
        boolean newVisibilityState = !previousVisibilityState;
        event.setPublic(newVisibilityState);
        event.setUpdatedAt(Instant.now());

        // Update details map if it exists
        if (event.getDetails() != null) {
            event.getDetails().put("isPublic", newVisibilityState);
        }

        event = eventRepository.save(event);
        eventSubject.notifyEventVisibilityChanged(event, newVisibilityState);
    }

    @Override
    public void undo() {
        if (event != null) {
            // Restore previous visibility state
            event = eventRepository.findById(eventId).orElse(null);
            if (event != null) {
                event.setPublic(previousVisibilityState);

                // Update details map if it exists
                if (event.getDetails() != null) {
                    event.getDetails().put("isPublic", previousVisibilityState);
                }

                event = eventRepository.save(event);
                eventSubject.notifyEventVisibilityChanged(event, previousVisibilityState);
            }
        }
    }

    public Event getEvent() {
        return event;
    }
}
