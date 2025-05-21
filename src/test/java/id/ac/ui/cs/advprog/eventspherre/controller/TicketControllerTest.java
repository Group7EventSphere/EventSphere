package id.ac.ui.cs.advprog.eventspherre.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.TicketService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private TicketTypeService ticketTypeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Ticket sampleTicket() {
        TicketType type = new TicketType("VIP", new BigDecimal("100.00"), 10);
        return new Ticket(type, mockUser(), "TKT-ABC123");
    }

    private User mockUser() {
        User user = new User();
        user.setId(1);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole(User.Role.ATTENDEE);
        return user;
    }

    @Test
    @DisplayName("GET /tickets/{id} returns ticket if found")
    void getTicketById_shouldReturnTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = sampleTicket();

        when(ticketService.getTicketById(ticketId)).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/tickets/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationCode").value("TKT-ABC123"));
    }

    @Test
    @DisplayName("GET /tickets/code/{code} returns ticket if found")
    void getTicketByConfirmationCode_shouldReturnTicket() throws Exception {
        String code = "TKT-ABC123";
        Ticket ticket = sampleTicket();

        when(ticketService.getTicketByConfirmationCode(code)).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/tickets/code/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationCode").value("TKT-ABC123"));
    }

    @Test
    @DisplayName("GET /tickets?attendeeId=1 returns user's tickets")
    void getTicketsByAttendee_shouldReturnList() throws Exception {
        when(ticketService.getTicketsByAttendeeId(1)).thenReturn(List.of(sampleTicket()));

        mockMvc.perform(get("/tickets/attendee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].confirmationCode").value("TKT-ABC123"));
    }

    @Test
    @DisplayName("POST /tickets creates ticket and returns JSON")
    void createTicket_shouldReturnCreatedTicket() throws Exception {
        Ticket ticket = sampleTicket();
        when(ticketService.createTicket(any(Ticket.class))).thenReturn(ticket);

        mockMvc.perform(post("/tickets")
                        .sessionAttr("loggedInUser", mockUser())  // ✅ add this line
                        .param("confirmationCode", "TKT-ABC123")
                        .param("ticketType.name", "VIP")
                        .param("ticketType.price", "100.00")
                        .param("ticketType.quota", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets"));

        verify(ticketService).createTicket(any(Ticket.class));
    }

    @Test
    @DisplayName("GET /tickets/create returns form view")
    void createTicketForm_shouldReturnView() throws Exception {
        when(ticketTypeService.findAll()).thenReturn(List.of(new TicketType("VIP", new BigDecimal("100.00"), 10)));

        mockMvc.perform(get("/tickets/create")
                        .sessionAttr("loggedInUser", mockUser()))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/create"))
                .andExpect(model().attributeExists("ticket"))
                .andExpect(model().attributeExists("ticketTypes"));
    }

    @Test
    @DisplayName("GET /tickets returns list view for logged-in user")
    void listTickets_shouldReturnView() throws Exception {
        when(ticketService.getTicketsByAttendeeId(1)).thenReturn(List.of(sampleTicket()));

        mockMvc.perform(get("/tickets")
                        .sessionAttr("loggedInUser", mockUser()))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/list"))
                .andExpect(model().attributeExists("tickets"));
    }

    @Test
    @DisplayName("POST /tickets (form) creates ticket and redirects")
    void createTicketFormSubmission_shouldRedirect() throws Exception {
        when(ticketService.createTicket(any(Ticket.class))).thenReturn(sampleTicket());

        mockMvc.perform(post("/tickets")
                        .sessionAttr("loggedInUser", mockUser())
                        .param("confirmationCode", "TKT-ABC123")
                        .param("ticketType.name", "VIP")
                        .param("ticketType.price", "100.00")
                        .param("ticketType.quota", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets"));

        verify(ticketService).createTicket(any(Ticket.class));
    }
}
