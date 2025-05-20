package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {
    // private Event event;
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Transient
    private User attendee;

    @Column(nullable = false, unique = true)
    private String confirmationCode;

    public Ticket() {}

    public Ticket(TicketType ticketType, User attendee, String confirmationCode) {
        this.ticketType = ticketType;
        this.attendee = attendee;
        this.userId = attendee.getId(); // assuming User has getId() returning int
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
