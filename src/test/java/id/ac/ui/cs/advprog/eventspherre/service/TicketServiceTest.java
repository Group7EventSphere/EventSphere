package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User user;
    private User adminUser;
    private Ticket sampleTicket;
    private UUID ticketId;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Test User");
        user.setRole(User.Role.ATTENDEE);

        adminUser = new User();
        adminUser.setId(99);
        adminUser.setName("Admin");
        adminUser.setRole(User.Role.ADMIN);

        TicketType type = new TicketType("VIP", new BigDecimal("100.00"), 100);

        sampleTicket = new Ticket(type, user, "TKT-123ABC");
        ticketId = sampleTicket.getId();
    }

    @Test
    @DisplayName("Should create ticket and decrement quota")
    void createTicket_shouldSaveTicketAndDecreaseQuota() {
        TicketType type = new TicketType("Standard", new BigDecimal("50.00"), 10);
        User user = new User();
        user.setId(5);

        Ticket ticket = new Ticket(type, user, "TKT-ABC123");

        when(ticketTypeRepository.findById(type.getId())).thenReturn(Optional.of(type));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket saved = ticketService.createTicket(ticket);

        assertThat(saved).isEqualTo(ticket);
        assertEquals(9, type.getQuota());
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Should fail when ticket quota is 0")
    void createTicket_quotaExceeded_throwsException() {
        TicketType ticketType = new TicketType("Sold Out", new BigDecimal("999.99"), 0);
        User user = new User();
        user.setId(11);
        user.setName("TooLate");
        user.setEmail("late@example.com");

        Ticket ticket = new Ticket(ticketType, user, "TKT-LATE");

        when(ticketTypeRepository.findById(ticketType.getId())).thenReturn(Optional.of(ticketType));

        assertThrows(IllegalStateException.class, () -> ticketService.createTicket(ticket));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when ticket type not found")
    void createTicket_missingTicketType_throwsException() {
        TicketType ticketType = new TicketType("Ghost", new BigDecimal("0.01"), 1);
        User user = new User();
        user.setId(88);

        Ticket ticket = new Ticket(ticketType, user, "TKT-GHOST");

        when(ticketTypeRepository.findById(ticketType.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketService.createTicket(ticket));
    }

    @Test
    @DisplayName("Should return ticket if exists")
    void getTicketById_shouldReturnTicket() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));

        Optional<Ticket> found = ticketService.getTicketById(ticketId);

        assertTrue(found.isPresent());
        assertEquals(sampleTicket, found.get());
    }

    @Test
    @DisplayName("Should return empty if ticket not found")
    void getTicketById_shouldReturnEmpty() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Ticket> result = ticketService.getTicketById(id);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should delete ticket by ID")
    void deleteTicket_shouldInvokeRepository() {
        ticketService.deleteTicket(ticketId);
        verify(ticketRepository).deleteById(ticketId);
    }

    @Test
    @DisplayName("Should return all tickets for an attendee")
    void getTicketsByAttendeeId_shouldReturnList() {
        when(ticketRepository.findAllByUserId(user.getId())).thenReturn(List.of(sampleTicket));

        List<Ticket> tickets = ticketService.getTicketsByAttendeeId(user.getId());

        assertEquals(1, tickets.size());
        assertEquals(sampleTicket, tickets.get(0));
    }

    @Test
    @DisplayName("Should return ticket by confirmation code")
    void getTicketByConfirmationCode_shouldReturnTicket() {
        when(ticketRepository.findByConfirmationCode("TKT-123ABC")).thenReturn(Optional.of(sampleTicket));

        Optional<Ticket> ticket = ticketService.getTicketByConfirmationCode("TKT-123ABC");

        assertTrue(ticket.isPresent());
        assertEquals(sampleTicket, ticket.get());
    }

    @Test
    @DisplayName("Should count tickets by type")
    void countTicketsByType_shouldReturnCorrectCount() {
        TicketType type = new TicketType("Early Bird", new BigDecimal("10.00"), 30);
        when(ticketRepository.countByTicketTypeId(type.getId())).thenReturn(5L);

        long count = ticketService.countTicketsByType(type.getId());

        assertEquals(5L, count);
    }
}
