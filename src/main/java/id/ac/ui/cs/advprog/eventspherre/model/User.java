package id.ac.ui.cs.advprog.eventspherre.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class User {

    private Integer id;
    private String name;
    private String email;
    private String password;
    private Date createdAt;
    private Date updatedAt;
    private Role role;
    private Double balance;

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

