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
        TicketType ticketType = new TicketType("VIP", new BigDecimal("75.00"), 20);
        assertEquals("VIP", ticketType.getName());
    }

    @Test
    void testGetPrice() {
        TicketType ticketType = new TicketType("VIP", new BigDecimal("120.00"), 5);
        assertEquals(new BigDecimal("120.00"), ticketType.getPrice());
    }

    @Test
    void testTicketTypeConstructorAndFields() {
        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 10);

        assertEquals("VIP", ticketType.getName());
        assertEquals(new BigDecimal("100.00"), ticketType.getPrice());
        assertEquals(10, ticketType.getQuota());
        assertNull(ticketType.getId()); // Because it hasn’t been persisted
    }

    @Test
    void testGetQuota() {
        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 13);
        assertEquals(13, ticketType.getQuota());
    }

    @Test
    void testReduceQuotaWhenTicketPurchased() {
        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 10);
        ticketType.reduceQuota(1);
        assertEquals(9, ticketType.getQuota());
    }

    @Test
    void testThrowExceptionWhenQuotaTooLow() {
        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 1);
        assertThrows(IllegalArgumentException.class, () -> ticketType.reduceQuota(2));
    }

    @Test
    void testUpdateTicketType() {
        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 10);
        ticketType.setName("Regular");
        assertEquals("Regular", ticketType.getName());
    }

    @Test
    void testUpdateTicketQuota() {
        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 10);
        ticketType.setQuota(25);
        assertEquals(25, ticketType.getQuota());
    }

    @Test
    void testUpdateTicketPrice() {
        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 10);
        ticketType.setPrice(new BigDecimal("150.00"));
        assertEquals(new BigDecimal("150.00"), ticketType.getPrice());
    }

    @Test
    void testOnlyOrganizerCanCreateTicketType() {
        User organizer = new User();
        organizer.setRole(User.Role.ORGANIZER);

        TicketType ticketType = TicketType.create("VIP", new BigDecimal("100.00"), 10, organizer);
        assertEquals("VIP", ticketType.getName());
    }

    @Test
    @DisplayName("reduceQuota - should throw when quantity is zero or negative")
    void reduceQuota_shouldThrow_whenQuantityIsZeroOrNegative() {
        IllegalArgumentException zeroException = assertThrows(IllegalArgumentException.class, () ->
                ticketType.reduceQuota(0)
        );
        assertEquals("Quantity must be positive", zeroException.getMessage());

        IllegalArgumentException negativeException = assertThrows(IllegalArgumentException.class, () ->
                ticketType.reduceQuota(-3)
        );
        assertEquals("Quantity must be positive", negativeException.getMessage());
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

