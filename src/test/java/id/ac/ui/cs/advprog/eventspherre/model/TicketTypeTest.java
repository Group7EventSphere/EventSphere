package id.ac.ui.cs.advprog.eventspherre.model;


import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class TicketTypeTest {
    @Test
    void testReduceQuotaWhenTicketPurchased() {
        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 10);
        ticketType.reduceQuota(1);
        assertEquals(9, ticketType.getQuota());
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
}

