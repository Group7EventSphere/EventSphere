package id.ac.ui.cs.advprog.eventspherre.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private TicketTypeService ticketTypeService;

    @MockBean
    private EventManagementService eventManagementService;

    @MockBean
    private UserService userService;

    private User mockUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setName("Test User");
        return user;
    }

    private Ticket sampleTicket() {
        TicketType type = new TicketType("VIP", new BigDecimal("100.00"), 10);
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setConfirmationCode("TKT-ABC123");
        ticket.setTicketType(type);
        ticket.setAttendee(mockUser());
        return ticket;
    }

    @Test
    @DisplayName("GET /tickets returns list view with user’s tickets")
    void listUserTickets_shouldReturnTicketListView() throws Exception {
        User user = mockUser();
        when(ticketService.getTicketsByAttendeeId(user.getId())).thenReturn(List.of(sampleTicket()));
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(get("/tickets").with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/list"))
                .andExpect(model().attributeExists("tickets"));
    }

    @Test
    @DisplayName("GET /tickets/{id} returns ticket detail view")
    void getTicketById_shouldReturnView() throws Exception {
        UUID id = UUID.randomUUID();
        when(ticketService.getTicketById(id)).thenReturn(Optional.of(sampleTicket()));

        mockMvc.perform(get("/tickets/" + id).with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/detail"))
                .andExpect(model().attributeExists("ticket"));
    }

    @Test
    @DisplayName("POST /tickets/create should create ticket and redirect")
    void createTicket_shouldRedirectAfterCreation() throws Exception {
        User user = mockUser();
        UUID ticketTypeId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setConfirmationCode("TKT-ABC123");

        // Mock user and ticketType
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        when(ticketTypeService.getTicketTypeById(ticketTypeId))
                .thenReturn(Optional.of(new TicketType("VIP", new BigDecimal("500000"), 10)));

        // Mock ticket creation result
        when(ticketService.createTicket(any(Ticket.class), eq(2)))
                .thenReturn(List.of(ticket));

        mockMvc.perform(post("/tickets/create")
                        .param("confirmationCode", "TKT-ABC123")
                        .param("ticketTypeId", ticketTypeId.toString())
                        .param("quota", "2")
                        .with(user("test@example.com"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets"));

        verify(ticketService).createTicket(any(Ticket.class), eq(2));
    }

    @Test
    @DisplayName("GET /tickets/code/{code} returns ticket")
    void getTicketByConfirmationCode_shouldReturnTicket() throws Exception {
        String code = "TKT-ABC123";
        when(ticketService.getTicketByConfirmationCode(code)).thenReturn(Optional.of(sampleTicket()));

        mockMvc.perform(get("/tickets/code/" + code).with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationCode").value("TKT-ABC123"));
    }

    @Test
    @DisplayName("GET /tickets/attendee/{attendeeId} returns user’s tickets")
    void getTicketsByAttendee_shouldReturnList() throws Exception {
        when(ticketService.getTicketsByAttendeeId(1)).thenReturn(List.of(sampleTicket()));

        mockMvc.perform(get("/tickets/attendee/1").with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].confirmationCode").value("TKT-ABC123"));
    }

    @Test
    @DisplayName("GET /tickets/count/{ticketTypeId} returns count")
    void countTicketsByType_shouldReturnCount() throws Exception {
        UUID ticketTypeId = UUID.randomUUID();
        when(ticketService.countTicketsByType(ticketTypeId)).thenReturn(5L);

        mockMvc.perform(get("/tickets/count/" + ticketTypeId).with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}