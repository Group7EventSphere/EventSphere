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
    @GeneratedValue(strategy = GenerationType.AUTO)
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
        }
        this.quota -= quantity;
    }
}
