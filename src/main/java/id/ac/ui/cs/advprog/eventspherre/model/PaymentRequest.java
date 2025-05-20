package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
@Entity
@Table(name = "payment_request")
public class PaymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PaymentType type;

    @Column(nullable = false)
    private boolean processed;

    @Column(length = 255)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Transient
    private User user;

    public PaymentRequest(User user, double amount, PaymentType type) {
    this.user     = user;
    this.userId   = user.getId();
    this.amount   = amount;
    this.type     = type;
    this.processed= false;
    this.createdAt= Instant.now();

    Integer maybeId = (user != null ? user.getId() : null);
    this.userId    = (maybeId != null ? maybeId : 0);
    }

    public PaymentType getPaymentType() { return this.type; }
    public void setPaymentType(PaymentType type) { this.type = type; }

    public enum PaymentType { TOPUP, PURCHASE }
}