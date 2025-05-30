package id.ac.ui.cs.advprog.eventspherre.model;

import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    void testTicketPriceFields() {
        // Test setting price fields
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal purchasePrice = new BigDecimal("90.00");
        BigDecimal discountPercentage = new BigDecimal("10.00");
        
        ticket.setOriginalPrice(originalPrice);
        ticket.setPurchasePrice(purchasePrice);
        ticket.setDiscountPercentage(discountPercentage);
        
        assertEquals(originalPrice, ticket.getOriginalPrice());
        assertEquals(purchasePrice, ticket.getPurchasePrice());
        assertEquals(discountPercentage, ticket.getDiscountPercentage());
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
    @DisplayName("Ticket constructor - should set userId = 0 when attendee is null")
    void constructor_shouldSetUserIdZero_whenAttendeeIsNull() {
        Ticket result = new Ticket(ticketType, null, confirmationCode);

        assertEquals(0, result.getUserId());
    }

    @Test
    @DisplayName("Ticket constructor - should set userId = 0 when attendee ID is null")
    void constructor_shouldSetUserIdZero_whenAttendeeIdIsNull() {
        User noIdUser = new User();  // no setId()
        Ticket result = new Ticket(ticketType, noIdUser, confirmationCode);

        assertEquals(0, result.getUserId());
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
    
    @Test
    @DisplayName("Ticket - should have createdAt field")
    void testCreatedAtField() {
        // Test that createdAt field exists and can be set
        LocalDateTime testTime = LocalDateTime.now();
        ticket.setCreatedAt(testTime);
        
        assertEquals(testTime, ticket.getCreatedAt());
        
        // @CreationTimestamp will automatically set this when persisted to DB
        // so we don't need to test the @PrePersist behavior in unit tests
    }
}
