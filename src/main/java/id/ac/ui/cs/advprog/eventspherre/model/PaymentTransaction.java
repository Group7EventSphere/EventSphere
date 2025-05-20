package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PaymentRequest.PaymentType type;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    @Transient
    private User user;

    public PaymentTransaction(User user,
                              double amount,
                              PaymentRequest.PaymentType type,
                              String status) {
        this.user      = user;
        this.userId    = user.getId();
        this.amount    = amount;
        this.type      = type;
        this.status    = status;
        this.createdAt = Instant.now();
    }

    public PaymentRequest.PaymentType getPaymentType() {
        return this.type;
    }
    public void setPaymentType(PaymentRequest.PaymentType type) {
        this.type = type;
    }

    public User getUser() {
        return this.user;
    }
    public void setUser(User user) {
        this.user   = user;
        if (user != null) this.userId = user.getId();
    }
}
