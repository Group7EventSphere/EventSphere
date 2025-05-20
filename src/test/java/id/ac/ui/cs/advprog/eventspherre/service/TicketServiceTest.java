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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket sampleTicket;
    private UUID ticketId;
    private User user;
    private User adminUser;

    @BeforeEach
    void setUp() {
        User user = new User();
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
    void getTicketsByUserId_shouldReturnList() {
        when(ticketRepository.findAllByUserId(1)).thenReturn(Arrays.asList(sampleTicket));
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

    @Test
    @DisplayName("Should assign user, generate code, and reduce quota on ticket creation")
    void createTicket_checkoutBehavior() {
        // Given
        TicketType ticketType = new TicketType("Regular", new BigDecimal("50.00"), 5);

        User user = new User();
        user.setId(10);
        user.setName("CheckoutUser");
        user.setEmail("checkout@example.com");
        user.setRole(User.Role.ATTENDEE);

        Ticket ticket = new Ticket(ticketType, user, null); // no confirmation code

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID()); // simulate DB save
            return saved;
        });

        // When
        Ticket saved = ticketService.createTicket(ticket);

        // Then
        assertNotNull(saved.getConfirmationCode());
        assertTrue(saved.getConfirmationCode().startsWith("TKT-"));
        assertEquals(10, saved.getUserId());
        assertEquals(4, ticketType.getQuota()); // quota reduced

        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should fail when ticket quota is 0")
    void createTicket_quotaExceeded_throwsException() {
        TicketType ticketType = new TicketType("Sold Out", new BigDecimal("999.99"), 0);

        User user = new User();
        user.setId(11);
        user.setName("TooLate");
        user.setEmail("late@example.com");

        Ticket ticket = new Ticket(ticketType, user, null);

        assertThrows(IllegalStateException.class, () -> ticketService.createTicket(ticket));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not allow quota to become negative")
    void createTicket_preventsNegativeQuota() {
        TicketType type = new TicketType("Last-Minute", new BigDecimal("90.00"), 1);
        User user = new User();
        user.setId(7);

        Ticket first = new Ticket(type, user, null);
        ticketService.createTicket(first);

        Ticket second = new Ticket(type, user, null);

        assertThrows(IllegalStateException.class, () -> {
            ticketService.createTicket(second);
        });

        assertEquals(0, type.getQuota());
    }

    @Test
    @DisplayName("Should not override confirmation code if already provided")
    void createTicket_preservesProvidedConfirmationCode() {
        TicketType ticketType = new TicketType("Gold", new BigDecimal("120.00"), 3);
        User user = new User();
        user.setId(5);

        String customCode = "CUSTOM-1234";
        Ticket ticket = new Ticket(ticketType, user, customCode);

        when(ticketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket saved = ticketService.createTicket(ticket);

        assertEquals(customCode, saved.getConfirmationCode());
        verify(ticketRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw when attendee is null")
    void createTicket_throwsIfAttendeeNull() {
        TicketType type = new TicketType("Silver", new BigDecimal("70.00"), 5);

        Ticket ticket = new Ticket(); // empty constructor
        ticket.setTicketType(type);
        ticket.setAttendee(null); // explicit

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.createTicket(ticket);
        });

        assertTrue(ex.getMessage().contains("Attendee must be specified"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when attendee has no ID")
    void createTicket_throwsIfAttendeeIdMissing() {
        TicketType type = new TicketType("Bronze", new BigDecimal("30.00"), 10);
        User user = new User(); // ID is null

        Ticket ticket = new Ticket(type, user, null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.createTicket(ticket);
        });

        assertTrue(ex.getMessage().contains("Attendee must be specified"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should populate ticket with user ID and save")
    void createTicket_setsFieldsCorrectlyBeforeSave() {
        TicketType type = new TicketType("Regular", new BigDecimal("50.00"), 3);
        User user = new User();
        user.setId(99);
        user.setName("Verify");
        user.setEmail("verify@example.com");

        Ticket ticket = new Ticket(type, user, null);

        when(ticketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket saved = ticketService.createTicket(ticket);

        assertEquals(99, saved.getUserId());
        assertEquals(type, saved.getTicketType());
        assertEquals(2, type.getQuota()); // reduced
    }
}
