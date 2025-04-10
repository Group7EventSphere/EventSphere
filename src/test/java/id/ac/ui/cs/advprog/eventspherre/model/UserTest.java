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
        Date now = new Date();
        User.Role role = User.Role.ORGANIZER;

        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setRole(role);

        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
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
}
