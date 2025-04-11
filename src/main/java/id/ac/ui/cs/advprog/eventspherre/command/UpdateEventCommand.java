package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import java.util.Map;

public class UpdateEventCommand implements Command {
    private Event event;
    private Map<String, Object> updatedDetails;

    public UpdateEventCommand(Event event, Map<String, Object> updatedDetails) {
        this.event = event;
        this.updatedDetails = updatedDetails;
    }

    @Override
    public void execute() {
        event.setDetails(updatedDetails);
    }
}
