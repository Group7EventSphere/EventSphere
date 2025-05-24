package id.ac.ui.cs.advprog.eventspherre.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TicketTypeTest {
    private TicketType ticketType;

    @BeforeEach
    void setUp() {
        ticketType = new TicketType("VIP", new BigDecimal("120.00"), 5);
    }

    @Test
    void testGetName() {
        assertEquals("VIP", ticketType.getName());
    }

    @Test
    void testGetPrice() {
        assertEquals(new BigDecimal("120.00"), ticketType.getPrice());
    }

    @Test
    void testTicketTypeConstructorAndFields() {
        TicketType customTicketType = new TicketType("VIP", new BigDecimal("100.00"), 10);

        assertEquals("VIP", customTicketType.getName());
        assertEquals(new BigDecimal("100.00"), customTicketType.getPrice());
        assertEquals(10, customTicketType.getQuota());
        assertNull(customTicketType.getId()); // Because it hasn’t been persisted
    }

    @Test
    void testGetQuota() {
        assertEquals(5, ticketType.getQuota());
    }

    @Test
    void testReduceQuotaWhenTicketPurchased() {
        ticketType.reduceQuota(1);
        assertEquals(4, ticketType.getQuota());
    }

    @Test
    void testThrowExceptionWhenQuotaTooLow() {
        ticketType.reduceQuota(4); // Now quota = 1
        assertThrows(IllegalArgumentException.class, () -> ticketType.reduceQuota(2));
    }

    @Test
    void testUpdateTicketType() {
        ticketType.setName("Regular");
        assertEquals("Regular", ticketType.getName());
    }

    @Test
    void testUpdateTicketQuota() {
        ticketType.setQuota(25);
        assertEquals(25, ticketType.getQuota());
    }

    @Test
    void testUpdateTicketPrice() {
        ticketType.setPrice(new BigDecimal("150.00"));
        assertEquals(new BigDecimal("150.00"), ticketType.getPrice());
    }

    @Test
    void testOnlyOrganizerCanCreateTicketType() {
        User organizer = new User();
        organizer.setRole(User.Role.ORGANIZER);

        TicketType createdTicketType = TicketType.create("VIP", new BigDecimal("100.00"), 10, organizer);
        assertEquals("VIP", createdTicketType.getName());
    }

    @Test
    @DisplayName("reduceQuota - should throw when quantity is zero or negative")
    void reduceQuota_shouldThrow_whenQuantityIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> ticketType.reduceQuota(0));
        assertThrows(IllegalArgumentException.class, () -> ticketType.reduceQuota(-1));
    }

    @Test
    void testAttendeeCannotCreateTicketType() {
        User attendee = new User();
        attendee.setRole(User.Role.ATTENDEE);

        String name = "VIP";
        BigDecimal price = new BigDecimal("100.00");
        int quota = 10;

        assertThrows(IllegalArgumentException.class, () ->
                TicketType.create(name, price, quota, attendee)
        );
    }

}

