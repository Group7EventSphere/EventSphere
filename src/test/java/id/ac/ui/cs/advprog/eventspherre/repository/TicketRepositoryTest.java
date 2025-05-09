package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
// auto-configure an embedded test database (H2) for you
@AutoConfigureTestDatabase
// point JPA at your repo package
@EnableJpaRepositories(basePackageClasses = TicketRepository.class)
// point JPA at your entity package
@EntityScan(basePackageClasses = Ticket.class)
public class TicketRepositoryTest {

    @Autowired
    TicketRepository ticketRepository;

    @Test
    void testSaveAndFindTicket() {
        User attendee = new User();
        attendee.setId(1);
        attendee.setName("John Doe");
        attendee.setEmail("john@example.com");
        attendee.setRole(User.Role.ATTENDEE);

        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 10);

        Ticket ticket = new Ticket(ticketType, attendee, "TKT-ABC123");
        ticketRepository.save(ticket);

        Ticket found = ticketRepository.findById(ticket.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(ticket.getTicketType(), found.getTicketType());
        assertEquals(ticket.getAttendee(), found.getAttendee());
        assertEquals(ticket.getConfirmationCode(), found.getConfirmationCode());
    }

    @Test
    void testFindAllTicketsByAttendeeId() {
        User attendee = new User();
        attendee.setId(2);
        attendee.setName("Jane Doe");
        attendee.setEmail("jane@example.com");
        attendee.setRole(User.Role.ATTENDEE);

        TicketType ticketType = new TicketType("Regular", new BigDecimal("50.00"), 20);

        Ticket ticket1 = new Ticket(ticketType, attendee, "TKT-001");
        Ticket ticket2 = new Ticket(ticketType, attendee, "TKT-002");

        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        List<Ticket> tickets = ticketRepository.findAllByAttendeeId(attendee.getId());

        assertEquals(2, tickets.size());
    }

    @Test
    void testDeleteTicketById() {
        User attendee = new User();
        attendee.setId(3);
        attendee.setName("Mark Smith");
        attendee.setEmail("mark@example.com");
        attendee.setRole(User.Role.ATTENDEE);

        TicketType ticketType = new TicketType("Standard", new BigDecimal("75.00"), 30);

        Ticket ticket = new Ticket(ticketType, attendee, "TKT-003");
        ticketRepository.save(ticket);

        UUID ticketId = ticket.getId();
        ticketRepository.deleteById(ticketId);

        assertFalse(ticketRepository.findById(ticketId).isPresent());
    }

    @Test
    void testUpdateTicketConfirmationCode() {
        User attendee = new User();
        attendee.setId(4);
        attendee.setName("Emma Johnson");
        attendee.setEmail("emma@example.com");
        attendee.setRole(User.Role.ATTENDEE);

        TicketType ticketType = new TicketType("Early Bird", new BigDecimal("40.00"), 5);
        Ticket ticket = new Ticket(ticketType, attendee, "TKT-INIT");
        ticketRepository.save(ticket);

        Ticket saved = ticketRepository.findById(ticket.getId()).orElseThrow();
        saved.setConfirmationCode("TKT-UPDATED");
        ticketRepository.save(saved);

        Ticket updated = ticketRepository.findById(ticket.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals("TKT-UPDATED", updated.getConfirmationCode());
    }

    @Test
    void testFindByConfirmationCode() {
        User attendee = new User();
        attendee.setId(5);
        attendee.setName("Alice Wonderland");
        attendee.setEmail("alice@example.com");
        attendee.setRole(User.Role.ATTENDEE);

        TicketType ticketType = new TicketType("Premium", new BigDecimal("150.00"), 8);
        Ticket ticket = new Ticket(ticketType, attendee, "TKT-ALICE");
        ticketRepository.save(ticket);

        Optional<Ticket> found = ticketRepository.findByConfirmationCode("TKT-ALICE");
        assertTrue(found.isPresent());
        assertEquals("TKT-ALICE", found.get().getConfirmationCode());
    }

    @Test
    void testCountByTicketTypeId() {
        User attendee = new User();
        attendee.setId(6);
        attendee.setName("Bob Builder");
        attendee.setEmail("bob@example.com");
        attendee.setRole(User.Role.ATTENDEE);

        TicketType ticketType = new TicketType("Standard", new BigDecimal("75.00"), 30);
        Ticket ticket1 = new Ticket(ticketType, attendee, "TKT-BOB1");
        Ticket ticket2 = new Ticket(ticketType, attendee, "TKT-BOB2");

        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        long count = ticketRepository.countByTicketTypeId(ticketType.getId());
        assertEquals(2, count);
    }
}
