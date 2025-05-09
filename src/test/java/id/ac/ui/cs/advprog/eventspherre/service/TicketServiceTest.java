package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket sampleTicket;
    private UUID ticketId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1);
        user.setName("Test User");
        user.setRole(User.Role.ATTENDEE);

        TicketType type = new TicketType("VIP", new BigDecimal("100.00"), 100);

        sampleTicket = new Ticket(type, user, "TKT-123ABC");
        ticketId = sampleTicket.getId();
    }

    @Test
    @DisplayName("Create ticket should save and return the ticket")
    void createTicket_shouldReturnSavedTicket() {
        when(ticketRepository.save(sampleTicket)).thenReturn(sampleTicket);
        Ticket created = ticketService.createTicket(sampleTicket);
        assertThat(created).isEqualTo(sampleTicket);
    }

    @Test
    @DisplayName("Get ticket by ID should return the ticket")
    void getTicketById_shouldReturnTicket() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
        Optional<Ticket> result = ticketService.getTicketById(ticketId);
        assertThat(result).isPresent().contains(sampleTicket);
    }

    @Test
    @DisplayName("Get tickets by attendee ID should return ticket list")
    void getTicketsByAttendeeId_shouldReturnList() {
        when(ticketRepository.findAllByAttendeeId(1)).thenReturn(Arrays.asList(sampleTicket));
        assertThat(ticketService.getTicketsByAttendeeId(1)).containsExactly(sampleTicket);
    }

    @Test
    @DisplayName("Delete ticket should call repository delete")
    void deleteTicket_shouldDeleteById() {
        ticketService.deleteTicket(ticketId);
        verify(ticketRepository, times(1)).deleteById(ticketId);
    }

    @Test
    @DisplayName("Find by confirmation code should return ticket")
    void getTicketByConfirmationCode_shouldReturnTicket() {
        when(ticketRepository.findByConfirmationCode("TKT-123ABC"))
                .thenReturn(Optional.of(sampleTicket));
        Optional<Ticket> result = ticketService.getTicketByConfirmationCode("TKT-123ABC");
        assertThat(result).isPresent().contains(sampleTicket);
    }

    @Test
    @DisplayName("Count by ticket type ID should return correct count")
    void countTicketsByType_shouldReturnCount() {
        UUID typeId = sampleTicket.getTicketType().getId();
        when(ticketRepository.countByTicketTypeId(typeId)).thenReturn(3L);
        long count = ticketService.countTicketsByType(typeId);
        assertThat(count).isEqualTo(3L);
    }
}
