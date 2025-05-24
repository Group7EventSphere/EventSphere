package id.ac.ui.cs.advprog.eventspherre.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "ticket_types")
public class TicketType {
    @Id
    @JsonProperty("id")
    @GeneratedValue
    private UUID id;

    @JsonProperty("name")
    @Column(nullable = false, unique = true)
    private String name;

    @JsonProperty("price")
    @Column(nullable = false)
    private BigDecimal price;

    @JsonProperty("quota")
    @Column(nullable = false)
    private int quota;

    @Column(name = "event_id") // Renamed from event_uuid to event_id
    private Integer eventId; // Changed from String to int

    // Constructor
    public TicketType() {}

    public TicketType(String name, BigDecimal price, int quota) {
        this.name = name;
        this.price = price;
        this.quota = quota;
        this.eventId = eventId;
    }

    public void reduceQuota(int quantity) {
        if (quantity > this.quota) {
            throw new IllegalArgumentException("Not enough quota available");
        } else if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quota -= quantity;
    }

    public static TicketType create(String name, BigDecimal price, int quota, User user) {
        // Use Factory Method to handle creation of object
        if (user.getRole() != User.Role.ORGANIZER) {
            throw new IllegalArgumentException("Only organizers can create ticket types");
        }
        return new TicketType(name, price, quota);
    }

    public static TicketType create(String name, BigDecimal price, int quota, User user, int eventId) { // Changed eventId from UUID to int
        TicketType ticketType = create(name, price, quota, user);
        ticketType.setEventId(eventId); // Changed from setEventUuid to setEventId
        return ticketType;
    }
}
