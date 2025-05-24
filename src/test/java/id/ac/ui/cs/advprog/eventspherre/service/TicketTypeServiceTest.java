package id.ac.ui.cs.advprog.eventspherre.service;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketTypeServiceImpl ticketTypeService;

    private TicketType  ticketType;
    private User        organizer;
    private User        admin;
    private UUID        ticketTypeId;

    @BeforeEach
    void setUp() {
        ticketType = new TicketType("VIP", new BigDecimal("100.00"), 50);
        ticketTypeId = ticketType.getId();

        organizer = new User();
        organizer.setId(1);
        organizer.setRole(User.Role.ORGANIZER);

        admin = new User();
        admin.setId(2);
        admin.setRole(User.Role.ADMIN);
    }

    @Test
    @DisplayName("create - should create TicketType and save it to repository")
    void create_shouldCreateAndSaveTicketType() {
        String name = ticketType.getName();
        BigDecimal price = ticketType.getPrice();
        int quota = ticketType.getQuota();
        int eventId = 99;

        TicketType createdTicketType = TicketType.create(name, price, quota, organizer, eventId);
        when(ticketTypeRepository.save(any(TicketType.class))).thenReturn(createdTicketType);

        TicketType result = ticketTypeService.create(name, price, quota, organizer, eventId);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(price, result.getPrice());
        assertEquals(quota, result.getQuota());
        assertEquals(eventId, result.getEventId());

        verify(ticketTypeRepository, times(1)).save(any(TicketType.class));
    }

    @Test
    @DisplayName("Organizer can create a ticket type")
    void createTicketType_shouldSucceedForOrganizer() {
        TicketType created = TicketType.create("VIP", new BigDecimal("100.00"), 50, organizer);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo(ticketType.getName());
        assertThat(created.getQuota()).isEqualTo(ticketType.getQuota());
        assertThat(created.getPrice()).isEqualByComparingTo(ticketType.getPrice());
    }

    @Test
    @DisplayName("Non-organizer cannot create ticket type")
    void createTicketType_shouldFailForAttendee() {
        User attendee = new User();
        attendee.setRole(User.Role.ATTENDEE);

    assertThatThrownBy(() -> TicketType.create("VIP", new BigDecimal("100.00"), 10, attendee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only organizers can create ticket types");
    }

    @Test
    @DisplayName("Update ticket type should update fields if allowed")
    void updateTicketType_shouldUpdateFields() {
        TicketType updated = new TicketType("VIP Premium", new BigDecimal("150.00"), 100);
        when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));
        when(ticketTypeRepository.save(any(TicketType.class))).thenReturn(updated);

        TicketType result = ticketTypeService.updateTicketType(ticketTypeId, updated, organizer);
        assertThat(result.getName()).isEqualTo("VIP Premium");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("150.00"));
        assertThat(result.getQuota()).isEqualTo(100);
    }

    @Test
    @DisplayName("updateTicketType - should throw when TicketType not found")
    void updateTicketType_shouldThrow_whenTicketTypeNotFound() {
        when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ticketTypeService.updateTicketType(ticketTypeId, ticketType, organizer)
        );

        assertEquals("TicketType not found", exception.getMessage());
        verify(ticketTypeRepository, times(1)).findById(ticketTypeId);
        verify(ticketTypeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Admin can delete a ticket type")
    void deleteTicketType_shouldSucceedForAdmin() {
        User admin = new User();
        admin.setRole(User.Role.ADMIN);

        ticketTypeService.deleteTicketType(ticketTypeId, admin);

        verify(ticketService, times(1)).deleteTicketsByTicketTypeId(ticketTypeId);
        verify(ticketTypeRepository, times(1)).deleteById(ticketTypeId);
    }

    @Test
    @DisplayName("Non-admin cannot delete a ticket type")
    void deleteTicketType_shouldFailForNonAdmin() {
        assertThatThrownBy(() -> ticketTypeService.deleteTicketType(ticketTypeId, organizer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only admins can delete ticket types");
    }

    @Test
    @DisplayName("deleteTicketType - should throw if tickets still exist for the ticket type")
    void deleteTicketType_shouldThrow_whenTicketsStillExist() {
        doNothing().when(ticketService).deleteTicketsByTicketTypeId(ticketTypeId);
        when(ticketRepository.existsByTicketTypeId(ticketTypeId)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                ticketTypeService.deleteTicketType(ticketTypeId, admin)
        );

        assertEquals("Cannot delete ticket type with existing tickets.", exception.getMessage());

        verify(ticketService).deleteTicketsByTicketTypeId(ticketTypeId);
        verify(ticketRepository).existsByTicketTypeId(ticketTypeId);
    }

    @Test
    @DisplayName("Find ticket type by ID")
    void getTicketTypeById_shouldReturnResult() {
        when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));
        Optional<TicketType> found = ticketTypeService.getTicketTypeById(ticketTypeId);
        assertThat(found).isPresent().contains(ticketType);
    }

    @Test
    @DisplayName("Should return all ticket types")
    void testFindAllReturnsList() {
        when(ticketTypeRepository.findAll()).thenReturn(List.of(
                new TicketType("VIP", new BigDecimal("100.00"), 10),
                new TicketType("Regular", new BigDecimal("50.00"), 100)
        ));

        List<TicketType> result = ticketTypeService.findAll();
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findByEventId - should return list of ticket types")
    void findByEventId_shouldReturnTicketTypes() {
        // Success
        List<TicketType> expectedList = List.of(ticketType);
        when(ticketTypeRepository.findByEventId(1)).thenReturn(expectedList);

        List<TicketType> result = ticketTypeService.findByEventId(1);

        assertEquals(expectedList, result);
        verify(ticketTypeRepository, times(1)).findByEventId(1);
    }

    @Test
    @DisplayName("associateWithEvent - should set eventId and save ticket type")
    void associateWithEvent_shouldSetEventIdAndSave() {
        // Not found
        when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));

        ticketTypeService.associateWithEvent(ticketTypeId, 123);

        assertEquals(123, ticketType.getEventId());
        verify(ticketTypeRepository).save(ticketType);
    }

    @Test
    @DisplayName("associateWithEvent - should throw when ticket type not found")
    void associateWithEvent_shouldThrow_whenTicketTypeNotFound() {
        when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ticketTypeService.associateWithEvent(ticketTypeId, 123)
        );

        assertEquals("TicketType not found", exception.getMessage());
        verify(ticketTypeRepository).findById(ticketTypeId);
        verify(ticketTypeRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTicketTypesByEventId - should return ticket types for event")
    void getTicketTypesByEventId_shouldReturnTicketTypes() {
        List<TicketType> expected = List.of(ticketType);
        when(ticketTypeRepository.findByEventId(1)).thenReturn(expected);

        List<TicketType> result = ticketTypeService.getTicketTypesByEventId(1);

        assertEquals(expected, result);
        verify(ticketTypeRepository).findByEventId(1);
    }
}
