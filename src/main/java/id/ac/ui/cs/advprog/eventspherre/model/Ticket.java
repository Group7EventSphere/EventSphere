package id.ac.ui.cs.advprog.eventspherre.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Ticket {
    // private Event event;
    private UUID id = UUID.randomUUID();
    private TicketType ticketType;
    private User attendee;
    private String confirmationCode;

    public Ticket() {}

    public Ticket(TicketType ticketType, User attendee, String confirmationCode) {
        this.ticketType = ticketType;
        this.attendee = attendee;
        this.confirmationCode = confirmationCode;
    }

    public void updateTicketType(TicketType newType, User byUser) {
        if (!isPrivileged(byUser)) {
            throw new SecurityException("Only organizers or admins can update ticket type.");
        }
        this.ticketType = newType;
    }

    public void updateConfirmationCode(String newCode, User byUser) {
        if (!isPrivileged(byUser)) {
            throw new SecurityException("Only organizers or admins can update confirmation code.");
        }
        this.confirmationCode = newCode;
    }

    private boolean isPrivileged(User user) {
        return user.getRole() == User.Role.ORGANIZER || user.getRole() == User.Role.ADMIN;
    }
}
