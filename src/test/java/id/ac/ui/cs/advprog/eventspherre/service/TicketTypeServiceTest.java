package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketTypeServiceTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @InjectMocks
    private TicketTypeServiceImpl ticketTypeService;

    private TicketType ticketType;
    private User organizer;
    private UUID ticketTypeId;

    @BeforeEach
    void setUp() {
        ticketType = new TicketType("VIP", new BigDecimal("100.00"), 50);
        ticketTypeId = ticketType.getId();

        organizer = new User();
        organizer.setId(1);
        organizer.setRole(User.Role.ORGANIZER);
    }

    @Test
    @DisplayName("Organizer can create a ticket type")
    void createTicketType_shouldSucceedForOrganizer() {
        when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
        TicketType result = ticketTypeService.createTicketType(ticketType, organizer);
        assertThat(result).isEqualTo(ticketType);
    }

    @Test
    @DisplayName("Non-organizer cannot create ticket type")
    void createTicketType_shouldFailForAttendee() {
        User attendee = new User();
        attendee.setRole(User.Role.ATTENDEE);
        assertThatThrownBy(() -> ticketTypeService.createTicketType(ticketType, attendee))
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
    @DisplayName("Admin can delete a ticket type")
    void deleteTicketType_shouldSucceedForAdmin() {
        User admin = new User();
        admin.setRole(User.Role.ADMIN);

        ticketTypeService.deleteTicketType(ticketTypeId, admin);
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
    @DisplayName("Find ticket type by ID")
    void getTicketTypeById_shouldReturnResult() {
        when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));
        Optional<TicketType> found = ticketTypeService.getTicketTypeById(ticketTypeId);
        assertThat(found).isPresent().contains(ticketType);
    }
}
