package id.ac.ui.cs.advprog.eventspherre.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TicketTypeTest {
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
    void testGetIdIsNotNull() {
        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 10);
        assertNotNull(ticketType.getId());
        assertInstanceOf(UUID.class, ticketType.getId());
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
}

