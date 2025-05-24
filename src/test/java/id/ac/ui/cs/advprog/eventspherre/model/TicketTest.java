package id.ac.ui.cs.advprog.eventspherre.model;

import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class TicketTest {
    private Ticket ticket;
    private TicketType ticketType;
    private User attendee;
    private String confirmationCode;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketType = new TicketType("VIP", new BigDecimal("120.00"), 5);

        attendee = new User();
        attendee.setId(1);
        attendee.setName("John Doe");
        attendee.setEmail("john@example.com");
        attendee.setRole(User.Role.ATTENDEE);

        confirmationCode = "TKT-ABC123";
        ticket = new Ticket(ticketType, attendee, confirmationCode);
    }

    @Test
    void testTicketCreation() {
        assertNull(ticket.getId());
        assertEquals(ticketType, ticket.getTicketType());
        assertEquals(attendee, ticket.getAttendee());
        assertEquals(confirmationCode, ticket.getConfirmationCode());
        assertEquals(attendee.getId(), ticket.getUserId());
    }

    @Test
    void testOrganizerCanUpdateTicketType() {
        User organizer = new User();
        organizer.setRole(User.Role.ORGANIZER);

        TicketType newType = new TicketType("Regular", new BigDecimal("80.00"), 15);
        ticket.updateTicketType(newType, organizer);

        assertEquals(newType, ticket.getTicketType());
    }

    @Test
    void testAdminCanUpdateConfirmationCode() {
        User admin = new User();
        admin.setRole(User.Role.ADMIN);

        ticket.updateConfirmationCode("TKT-NEW123", admin);

        assertEquals("TKT-NEW123", ticket.getConfirmationCode());
    }

    @Test
    void testAttendeeCannotUpdateTicketDetails() {
        User badUser = new User();
        badUser.setRole(User.Role.ATTENDEE);

        TicketType newType = new TicketType("Fake", new BigDecimal("1.00"), 1);

        assertThrows(SecurityException.class, () -> ticket.updateTicketType(newType, badUser));
        assertThrows(SecurityException.class, () -> ticket.updateConfirmationCode("TKT-FAKE000", badUser));
    }
}
