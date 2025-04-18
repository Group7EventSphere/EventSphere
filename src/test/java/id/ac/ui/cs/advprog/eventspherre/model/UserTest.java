package id.ac.ui.cs.advprog.eventspherre.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserGettersAndSetters() {
        User user = new User();
        Integer id = 1;
        String name = "Damar";
        String email = "damar@example.com";
        String password = "securepassword";
        Double balance = 500.0;
        Date now = new Date();
        User.Role role = User.Role.ORGANIZER;

        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setBalance(balance);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setRole(role);

        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(balance, user.getBalance());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
        assertEquals(role, user.getRole());
    }

    @Test
    void testRoleEnumValues() {
        assertEquals(User.Role.ADMIN, User.Role.valueOf("ADMIN"));
        assertEquals(User.Role.ORGANIZER, User.Role.valueOf("ORGANIZER"));
        assertEquals(User.Role.ATTENDEE, User.Role.valueOf("ATTENDEE"));
    }

    @Test
    void testTopUpNegativeAmountDoesNotChangeBalance() {
        User user = new User();
        user.setBalance(100.0);
        double initialBalance = user.getBalance();

        // Try to top-up a negative amount
        user.topUp(-50.0);

        // Expect: balance remains unchanged
        assertEquals(initialBalance, user.getBalance(), 0.001,
                "Top-up with a negative amount should not change the balance");
    }

    @Test
    void testDeductNegativeAmountDoesNotChangeBalance() {
        User user = new User();
        user.setBalance(100.0);
        double initialBalance = user.getBalance();

        // Try to deduct a negative amount
        boolean result = user.deduct(-50.0);

        // Expect: deduction should be rejected and balance remains unchanged
        assertFalse(result, "Deduction of a negative amount should be rejected");
        assertEquals(initialBalance, user.getBalance(), 0.001,
                "Deducting a negative amount should not change the balance");
    }

}
