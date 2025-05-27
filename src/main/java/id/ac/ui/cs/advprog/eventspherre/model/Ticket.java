package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.util.UUID;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {
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

    @Column(nullable = false)
    private String status = "pending";

    @Column(nullable = false)
    @DateTimeFormat(pattern = "dd MMM yyyy")
    private LocalDate date;

    @Column(name = "transaction_id")
    private UUID transactionId;
    
    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;
    
    @Column(name = "original_price")
    private BigDecimal originalPrice;
    
    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Ticket() {}

    public Ticket(TicketType ticketType, User attendee, String confirmationCode) {
        this.ticketType = ticketType;
        this.attendee = attendee;        // Defensive check to prevent NPE
        if (attendee == null || attendee.getId() == null) {
            this.userId = AppConstants.DEFAULT_USER_ID;
        } else {
            this.userId = attendee.getId();
        }

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
