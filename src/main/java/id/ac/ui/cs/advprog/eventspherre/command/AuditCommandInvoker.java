package id.ac.ui.cs.advprog.eventspherre.command;

import org.springframework.stereotype.Component;


@Component
public class AuditCommandInvoker {
    public void invoke(AuditCommand command) {
        command.execute();
    }
}