package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.sql.init.mode=never")
public class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    private TicketType saveTicketType(String name, BigDecimal price, int quantity) {
        TicketType type = new TicketType(name, price, quantity);
        return ticketTypeRepository.save(type);
    }

    private User createUser(int id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setRole(User.Role.ATTENDEE);
        return user;
    }

    @Test
    @DisplayName("Save and find Ticket by ID")
    void testSaveAndFindTicket() {
        TicketType type = saveTicketType("VIP", new BigDecimal("100.00"), 10);
        User attendee = createUser(1, "John Doe", "john@example.com");
        Ticket ticket = new Ticket(type, attendee, "TKT-ABC123");

        Ticket saved = ticketRepository.save(ticket);
        Ticket found = ticketRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(type, found.getTicketType());
        assertEquals(attendee.getId(), found.getUserId());
        assertEquals("TKT-ABC123", found.getConfirmationCode());
    }

    @Test
    @DisplayName("Find all Tickets by userId")
    void testFindAllTicketsByUserId() {
        TicketType type = saveTicketType("Regular", new BigDecimal("50.00"), 20);
        User attendee = createUser(2, "Jane Doe", "jane@example.com");

        Ticket ticket1 = new Ticket(type, attendee, "TKT-001");
        Ticket ticket2 = new Ticket(type, attendee, "TKT-002");

        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        List<Ticket> tickets = ticketRepository.findAllByUserId(2);
        assertEquals(2, tickets.size());
    }

    @Test
    @DisplayName("Delete Ticket by ID")
    void testDeleteTicketById() {
        TicketType type = saveTicketType("Standard", new BigDecimal("75.00"), 30);
        User attendee = createUser(3, "Mark Smith", "mark@example.com");

        Ticket ticket = new Ticket(type, attendee, "TKT-003");
        Ticket saved = ticketRepository.save(ticket);

        UUID ticketId = saved.getId();
        ticketRepository.deleteById(ticketId);

        assertFalse(ticketRepository.findById(ticketId).isPresent());
    }

    @Test
    @DisplayName("Update Ticket confirmation code")
    void testUpdateTicketConfirmationCode() {
        TicketType type = saveTicketType("Early Bird", new BigDecimal("40.00"), 5);
        User attendee = createUser(4, "Emma Johnson", "emma@example.com");

        Ticket ticket = new Ticket(type, attendee, "TKT-INIT");
        Ticket saved = ticketRepository.save(ticket);

        saved.setConfirmationCode("TKT-UPDATED");
        Ticket updated = ticketRepository.save(saved);

        assertNotNull(updated);
        assertEquals("TKT-UPDATED", updated.getConfirmationCode());
    }

    @Test
    @DisplayName("Find Ticket by confirmation code")
    void testFindByConfirmationCode() {
        TicketType type = saveTicketType("Premium", new BigDecimal("150.00"), 8);
        User attendee = createUser(5, "Alice Wonderland", "alice@example.com");

        Ticket ticket = new Ticket(type, attendee, "TKT-ALICE");
        ticketRepository.save(ticket);

        Optional<Ticket> found = ticketRepository.findByConfirmationCode("TKT-ALICE");

        assertTrue(found.isPresent());
        assertEquals("TKT-ALICE", found.get().getConfirmationCode());
    }

    @Test
    @DisplayName("Count Tickets by TicketType ID")
    void testCountByTicketTypeId() {
        TicketType type = saveTicketType("Standard", new BigDecimal("75.00"), 30);
        User attendee = createUser(6, "Bob Builder", "bob@example.com");

        Ticket t1 = new Ticket(type, attendee, "TKT-BOB1");
        Ticket t2 = new Ticket(type, attendee, "TKT-BOB2");

        ticketRepository.save(t1);
        ticketRepository.save(t2);

        long count = ticketRepository.countByTicketTypeId(type.getId());
        assertEquals(2, count);
    }
}
