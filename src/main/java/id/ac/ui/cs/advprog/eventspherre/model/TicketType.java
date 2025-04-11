package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class TicketType {
    // Getters & Setters
    @Id
    private UUID id = UUID.randomUUID();

    @Setter
    private String name;

    @Setter
    private BigDecimal price;

    @Setter
    private int quota;

    // Constructor
    public TicketType() {}

    public TicketType(String name, BigDecimal price, int quota) {
        this.name = name;
        this.price = price;
        this.quota = quota;
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
}
