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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User    user;
    private User    adminUser;
    private Ticket  sampleTicket;
    private UUID    ticketId;

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
        // Given
        UUID typeId = UUID.randomUUID();
        sampleTicket.getTicketType().setId(typeId);
        sampleTicket.getTicketType().setQuota(10);

        when(ticketTypeRepository.findById(typeId)).thenReturn(Optional.of(sampleTicket.getTicketType()));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        // When
        List<Ticket> savedTickets = ticketService.createTicket(sampleTicket, 1);

        // Then
        assertEquals(1, savedTickets.size());
        assertEquals(9, sampleTicket.getTicketType().getQuota()); // 10 - 1
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should fail when ticket quota is 0")
    void createTicket_quotaExceeded_throwsException() {
        // Given
        TicketType soldOutType = new TicketType("Sold Out", new BigDecimal("999.99"), 0);
        soldOutType.setId(UUID.randomUUID());
        Ticket soldOutTicket = new Ticket(soldOutType, user, "TKT-LATE");

        when(ticketTypeRepository.findById(soldOutType.getId())).thenReturn(Optional.of(soldOutType));

        // When & Then
        assertThrows(IllegalStateException.class, () -> ticketService.createTicket(soldOutTicket, 1));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when ticket type not found")
    void createTicket_missingTicketType_throwsException() {
        // Create fake ticket type that isn't saved in repo
        TicketType ghostType = new TicketType("Ghost", new BigDecimal("0.01"), 1);
        UUID ghostId = UUID.randomUUID();
        ghostType.setId(ghostId);

        Ticket ticket = new Ticket(ghostType, user, "TKT-GHOST");

        when(ticketTypeRepository.findById(ghostType.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketService.createTicket(ticket, 1));
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

    @Test
    @DisplayName("Should create multiple tickets and reduce quota")
    void createMultipleTickets_shouldSucceed() {
        // Setup ticket type with initial quota
        TicketType type = new TicketType();
        type.setId(UUID.randomUUID());
        type.setName("Standard");
        type.setPrice(new BigDecimal("100000"));
        type.setQuota(10); // starting quota

        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setName("Test User");
        mockUser.setEmail("test@example.com");

        // Prepare ticket input
        Ticket ticket = new Ticket();
        ticket.setTicketType(type);
        ticket.setAttendee(mockUser);

        // Mock repo behavior
        when(ticketTypeRepository.findById(type.getId())).thenReturn(Optional.of(type));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        // Execute
        List<Ticket> created = ticketService.createTicket(ticket, 3);

        // Assertions
        assertEquals(3, created.size());
        verify(ticketRepository, times(3)).save(any(Ticket.class));
        assertEquals(7, type.getQuota()); // 10 - 3
    }

    @Test
    @DisplayName("createTicket - should throw when attendee is null")
    void createTicket_shouldThrow_whenAttendeeIsNull() {
        Ticket ticket = new Ticket();
        ticket.setTicketType(sampleTicket.getTicketType());
        ticket.setAttendee(null); // null attendee

        when(ticketTypeRepository.findById(any())).thenReturn(Optional.of(sampleTicket.getTicketType()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ticketService.createTicket(ticket, 1)
        );

        assertEquals("Attendee must be specified with a valid user ID", exception.getMessage());
    }

    @Test
    @DisplayName("createTicket - should throw when attendee has null ID")
    void createTicket_shouldThrow_whenAttendeeIdIsNull() {
        User invalidUser = new User(); // id is null
        Ticket ticket = new Ticket();
        ticket.setTicketType(sampleTicket.getTicketType());
        ticket.setAttendee(invalidUser);

        when(ticketTypeRepository.findById(any())).thenReturn(Optional.of(sampleTicket.getTicketType()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ticketService.createTicket(ticket, 1)
        );

        assertEquals("Attendee must be specified with a valid user ID", exception.getMessage());
    }

    @Test
    @DisplayName("createTicket - should throw when attendee ID <= 0")
    void createTicket_shouldThrow_whenAttendeeIdIsZeroOrNegative() {
        User invalidUser = new User();
        invalidUser.setId(0); // invalid ID
        Ticket ticket = new Ticket();
        ticket.setTicketType(sampleTicket.getTicketType());
        ticket.setAttendee(invalidUser);

        when(ticketTypeRepository.findById(any())).thenReturn(Optional.of(sampleTicket.getTicketType()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ticketService.createTicket(ticket, 1)
        );

        assertEquals("Attendee must be specified with a valid user ID", exception.getMessage());
    }

    @Test
    @DisplayName("deleteTicketsByTicketTypeId - should call repository delete method")
    void deleteTicketsByTicketTypeId_shouldCallRepository() {
        UUID ticketTypeId = UUID.randomUUID();

        ticketService.deleteTicketsByTicketTypeId(ticketTypeId);

        verify(ticketRepository, times(1)).deleteByTicketTypeId(ticketTypeId);
    }

    @Test
    @DisplayName("updateTicket - should update and save when ticket exists")
    void updateTicket_shouldUpdateAndSave_whenTicketExists() {
        TicketType newType = new TicketType("Updated", new BigDecimal("150.00"), 50);
        Ticket updated = new Ticket(newType, user, "NEW-CODE");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket)); // Ticket found
        when(ticketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.updateTicket(ticketId, updated);

        assertEquals(newType, result.getTicketType());
        assertEquals("NEW-CODE", result.getConfirmationCode());
        verify(ticketRepository).save(sampleTicket);
    }

    @Test
    @DisplayName("updateTicket - should throw when ticket not found")
    void updateTicket_shouldThrow_whenTicketNotFound() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ticketService.updateTicket(ticketId, sampleTicket)
        );

        assertEquals("Ticket not found", ex.getMessage());
    }
}
