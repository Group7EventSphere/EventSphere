package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
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
    @DisplayName("GET /tickets returns list view with user’s tickets and events")
    void listUserTickets_shouldReturnTicketListView() throws Exception {
        User user = mockUser();

        TicketType ticketType = new TicketType();
        ticketType.setName("VIP");
        ticketType.setPrice(BigDecimal.valueOf(50000));
        ticketType.setEventId(1);

        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setTicketType(ticketType);
        ticket.setDate(LocalDate.now());
        ticket.setAttendee(user);

        Event event = new Event();
        event.setId(1);
        event.setTitle("Test Event");

        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        when(ticketService.getTicketsByAttendeeId(user.getId())).thenReturn(List.of(ticket));
        when(eventManagementService.getEvent(1)).thenReturn(event);

        mockMvc.perform(get("/tickets").with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/list"))
                .andExpect(model().attributeExists("ticketWithEventList"));
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

    @Test
    @DisplayName("Should show ticket selection page with event and ticket types")
    void showTicketSelection_shouldReturnSelectPage() throws Exception {
        int eventId = 1;

        User user = new User();
        user.setEmail("user@example.com");

        Event event = new Event();
        event.setId(eventId);
        event.setTitle("Sample Event");

        TicketType ticketType = new TicketType("VIP", new BigDecimal("100.00"), 50);
        List<TicketType> ticketTypes = List.of(ticketType);

        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(eventManagementService.getEvent(eventId)).thenReturn(event);
        when(ticketTypeService.findByEventId(eventId)).thenReturn(ticketTypes);

        mockMvc.perform(get("/tickets/select/{eventId}", eventId)
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/select"))
                .andExpect(model().attribute("event", event))
                .andExpect(model().attribute("ticketTypes", ticketTypes));
    }

    @Test
    @DisplayName("Should redirect to /events when event is not found")
    void showTicketSelection_shouldRedirectWhenEventNotFound() throws Exception {
        int eventId = 99;

        Principal principal = () -> "ghost@example.com";
        when(userService.getUserByEmail("ghost@example.com")).thenReturn(new User());
        when(eventManagementService.getEvent(eventId)).thenReturn(null);

        mockMvc.perform(get("/tickets/select/{eventId}", eventId)
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should redirect to ticket creation with parameters")
    void handleTicketSelection_shouldRedirectWithParams() throws Exception {
        UUID ticketTypeId = UUID.randomUUID();

        mockMvc.perform(post("/tickets/select")
                        .with(csrf())
                        .param("ticketTypeId", ticketTypeId.toString())
                        .param("quota", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets/create?ticketTypeId=" + ticketTypeId + "&quota=2"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should show ticket creation form with all model attributes")
    void showTicketForm_shouldLoadFormWithData() throws Exception {
        UUID ticketTypeId = UUID.randomUUID();

        // User
        User user = new User();
        user.setEmail("user@example.com");
        when(userService.getUserByEmail("user@example.com")).thenReturn(user);

        // Ticket Type
        TicketType ticketType = new TicketType("VIP", new BigDecimal("150.00"), 10);
        ticketType.setId(ticketTypeId);
        ticketType.setEventId(123);
        when(ticketTypeService.getTicketTypeById(ticketTypeId)).thenReturn(Optional.of(ticketType));

        // Event
        Event event = new Event();
        event.setId(123);
        event.setTitle("Spring Boot Workshop");
        event.setEventDate("2025-12-01T18:30"); // valid ISO_LOCAL_DATE_TIME format
        when(eventManagementService.getEvent(123)).thenReturn(event);

        mockMvc.perform(get("/tickets/create")
                        .param("ticketTypeId", ticketTypeId.toString())
                        .param("quota", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/create"))
                .andExpect(model().attributeExists("ticket"))
                .andExpect(model().attribute("quota", 3))
                .andExpect(model().attribute("ticketType", ticketType))
                .andExpect(model().attribute("event", event))
                .andExpect(model().attributeExists("eventDateFormatted"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should delete ticket successfully")
    void deleteTicket_shouldReturnNoContent() throws Exception {
        Ticket ticket = sampleTicket();
        UUID ticketId = ticket.getId();

        mockMvc.perform(delete("/tickets/" + ticketId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(ticketService, times(1)).deleteTicket(ticketId);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should update ticket and return updated object")
    void updateTicket_shouldReturnUpdatedTicket() throws Exception {
        Ticket updatedTicket = sampleTicket();
        UUID ticketId = updatedTicket.getId();

        when(userService.getUserByEmail("test@example.com")).thenReturn(mockUser());
        when(ticketService.updateTicket(eq(ticketId), any(Ticket.class))).thenReturn(updatedTicket);

        mockMvc.perform(put("/tickets/" + ticketId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "id": "%s",
                  "confirmationCode": "TKT-ABC123"
                }
                """.formatted(ticketId.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.confirmationCode").value("TKT-ABC123"));

        verify(ticketService).updateTicket(eq(ticketId), any(Ticket.class));
    }
}