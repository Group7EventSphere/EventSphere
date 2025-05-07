package id.ac.ui.cs.advprog.eventspherre.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.Date;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table (name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role= Role.ATTENDEE;

    @Column(name = "balance")
    private Double balance= 0.0;

    public enum Role {
        ADMIN, ORGANIZER, ATTENDEE
    }

    // Uses Domain Model pattern
    // Subject to change incase integration with other modules become difficult
    public void topUp(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public boolean deduct(double amount) {
        if (amount <= 0) {
            return false;
        }
        if (amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }
}

