package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.TicketService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    private Ticket sampleTicket() {
        User user = new User();
        user.setId(1);
        user.setName("Test User");
        user.setRole(User.Role.ATTENDEE);

        TicketType ticketType = new TicketType("Standard", new BigDecimal("50.00"), 10);

        return new Ticket(ticketType, user, "TKT-ABC123");
    }

    @Test
    @DisplayName("GET /EventSphere/tickets/{id} returns ticket if found")
    void getTicketById_shouldReturnTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = sampleTicket();
        when(ticketService.getTicketById(ticketId)).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/EventSphere/tickets/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationCode").value("TKT-ABC123"));
    }

    @Test
    @DisplayName("GET /EventSphere/tickets/{id} returns 404 if not found")
    void getTicketById_shouldReturnNotFound() throws Exception {
        UUID ticketId = UUID.randomUUID();
        when(ticketService.getTicketById(ticketId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/EventSphere/tickets/" + ticketId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /EventSphere/tickets/attendee/{attendeeId} returns list of tickets")
    void getTicketsByAttendee_shouldReturnList() throws Exception {
        when(ticketService.getTicketsByAttendeeId(1)).thenReturn(List.of(sampleTicket()));

        mockMvc.perform(get("/EventSphere/tickets/attendee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].confirmationCode").value("TKT-ABC123"));
    }

    @Test
    @DisplayName("DELETE /EventSphere/tickets/{id} should delete and return 204")
    void deleteTicket_shouldReturnNoContent() throws Exception {
        User user = new User();
        user.setId(1);
        user.setName("Dummy Admin");
        user.setRole(User.Role.ADMIN);

        UUID ticketId = UUID.randomUUID();
        doNothing().when(ticketService).deleteTicket(ticketId, user);

        mockMvc.perform(delete("/EventSphere/tickets/" + ticketId)
                        .param("userId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /EventSphere/tickets/code/{confirmationCode} returns ticket")
    void getTicketByConfirmationCode_shouldReturnTicket() throws Exception {
        Ticket ticket = sampleTicket();
        when(ticketService.getTicketByConfirmationCode("TKT-ABC123")).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/EventSphere/tickets/code/TKT-ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationCode").value("TKT-ABC123"));
    }

    @Test
    @DisplayName("GET /EventSphere/tickets/count/{ticketTypeId} returns ticket count")
    void countByTicketTypeId_shouldReturnCount() throws Exception {
        UUID ticketTypeId = UUID.randomUUID();
        when(ticketService.countTicketsByType(ticketTypeId)).thenReturn(5L);

        mockMvc.perform(get("/EventSphere/tickets/count/" + ticketTypeId))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}
