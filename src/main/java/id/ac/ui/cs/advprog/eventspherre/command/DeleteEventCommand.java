package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.observer.EventSubject;
import id.ac.ui.cs.advprog.eventspherre.repository.EventRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class DeleteEventCommand implements EventCommand {
    private final EventRepository eventRepository;
    private final EventSubject eventSubject;
    private final Integer eventId;

    private Event deletedEvent;

    @Override
    public void execute() {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            return;
        }

        deletedEvent = optionalEvent.get();
        eventRepository.delete(deletedEvent);
        eventSubject.notifyEventDeleted(deletedEvent);
    }

    @Override
    public void undo() {
        if (deletedEvent != null) {
            // Restore the deleted event
            Event restoredEvent = eventRepository.save(deletedEvent);
            eventSubject.notifyEventCreated(restoredEvent);
            deletedEvent = null;
        }
    }

    public Event getDeletedEvent() {
        return deletedEvent;
    }
}
