package id.ac.ui.cs.advprog.eventspherre.command;

import org.springframework.stereotype.Component;
import java.util.ArrayDeque;
import java.util.Deque;

@Component
public class EventCommandInvoker {
    private final Deque<EventCommand> commandHistory = new ArrayDeque<>();

    public void executeCommand(EventCommand command) {
        command.execute();
        commandHistory.push(command);
    }

    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            EventCommand lastCommand = commandHistory.pop();
            lastCommand.undo();
        }
    }

    public boolean hasCommandHistory() {
        return !commandHistory.isEmpty();
    }

    public void clearCommandHistory() {
        commandHistory.clear();
    }
}
