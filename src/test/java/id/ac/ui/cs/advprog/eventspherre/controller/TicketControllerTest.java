package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.WebSecurityTestConfig;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TicketController.class,
        excludeAutoConfiguration = ThymeleafAutoConfiguration.class
)
@Import(WebSecurityTestConfig.class)
@ActiveProfiles("test")
class TicketControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean TicketService            ticketService;
    @MockBean TicketTypeService        ticketTypeService;
    @MockBean UserService              userService;
    @MockBean EventManagementService   eventManagementService;
    @MockBean AuthenticationProvider   authenticationProvider;

    private User     attendee;
    private UUID     typeId;
    private Event    sampleEvent;
    private TicketType sampleType;

    @BeforeEach
    void setUp() {
        attendee = new User();
        attendee.setId(42);
        attendee.setEmail("attendee@example.com");

        typeId = UUID.randomUUID();
        sampleType = new TicketType();
        sampleType.setId(typeId);
        sampleType.setEventId(7);

        sampleEvent = new Event();
        sampleEvent.setId(7);
        sampleEvent.setEventDate("2025-06-01T10:00");
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    void showTicketSelection() throws Exception {
        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(eventManagementService.getEvent(7)).thenReturn(sampleEvent);
        when(ticketTypeService.findByEventId(7)).thenReturn(List.of(sampleType));

        mockMvc.perform(get("/tickets/select/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/select"))
                .andExpect(model().attributeExists("ticketTypes"))
                .andExpect(model().attribute("event", sampleEvent));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    void handleTicketSelection_redirectsToCreate() throws Exception {
        mockMvc.perform(post("/tickets/select")
                        .with(csrf())
                        .param("ticketTypeId", typeId.toString())
                        .param("quota", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/tickets/create?ticketTypeId=*"));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    void showTicketForm_populatesModel() throws Exception {
        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(ticketTypeService.getTicketTypeById(typeId)).thenReturn(Optional.of(sampleType));
        when(eventManagementService.getEvent(7)).thenReturn(sampleEvent);

        mockMvc.perform(get("/tickets/create")
                        .param("ticketTypeId", typeId.toString())
                        .param("quota", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/create"))
                .andExpect(model().attribute("quota", 2))
                .andExpect(model().attributeExists("ticket"))
                .andExpect(model().attribute("ticketType", sampleType))
                .andExpect(model().attribute("event", sampleEvent));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    void createTicket_invokesServiceAndRedirects() throws Exception {
        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(ticketTypeService.getTicketTypeById(typeId)).thenReturn(Optional.of(sampleType));
        when(ticketService.createTicket(any(Ticket.class), eq(5))).thenReturn(List.of(new Ticket()));

        mockMvc.perform(post("/tickets/create")
                        .with(csrf())
                        .param("ticketTypeId", typeId.toString())
                        .param("quota", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets"));
    }


    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    void getTicketById_returnsDetailView() throws Exception {
        UUID ticketId = UUID.randomUUID();
        Ticket t = new Ticket(); t.setId(ticketId);
        when(ticketService.getTicketById(ticketId)).thenReturn(Optional.of(t));

        mockMvc.perform(get("/tickets/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/detail"))
                .andExpect(model().attribute("ticket", t));
    }

    @Test
    void apiEndpoints_workCorrectly() throws Exception {
        when(ticketService.getTicketsByAttendeeId(42)).thenReturn(List.of(new Ticket()));
        when(ticketService.getTicketByConfirmationCode("CODE")).thenReturn(Optional.of(new Ticket()));
        when(ticketService.countTicketsByType(typeId)).thenReturn(7L);

        mockMvc.perform(get("/tickets/attendee/42")
                        .with(user("attendee@example.com").roles("ATTENDEE")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tickets/code/CODE")
                        .with(user("attendee@example.com").roles("ATTENDEE")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tickets/count/" + typeId)
                        .with(user("attendee@example.com").roles("ATTENDEE")))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    @DisplayName("Should redirect to /events if event not found")
    void showTicketSelection_shouldRedirectWhenEventNull() throws Exception {
        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(eventManagementService.getEvent(sampleType.getEventId())).thenReturn(null);

        mockMvc.perform(get("/tickets/select/{eventId}", sampleType.getEventId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    @DisplayName("Should return ticket/select view with model attributes")
    void showTicketSelection_shouldReturnViewAndModel() throws Exception {
        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(eventManagementService.getEvent(sampleType.getEventId())).thenReturn(sampleEvent);
        when(ticketTypeService.findByEventId(sampleType.getEventId())).thenReturn(List.of(sampleType));

        mockMvc.perform(get("/tickets/select/{eventId}", sampleType.getEventId()))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/select"))
                .andExpect(model().attribute("event", sampleEvent))
                .andExpect(model().attribute("ticketTypes", List.of(sampleType)));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    @DisplayName("Should throw IllegalArgumentException when ticket type is not found")
    void showTicketForm_shouldThrowWhenTicketTypeNotFound() throws Exception {
        UUID ticketTypeId = UUID.randomUUID();

        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(ticketTypeService.getTicketTypeById(ticketTypeId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/tickets/create")
                        .param("ticketTypeId", ticketTypeId.toString())
                        .param("quota", "2"))
                .andExpect(status().isInternalServerError()) // or use is4xxClientError if you're handling it
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof IllegalArgumentException))
                .andExpect(result -> assertEquals(
                        "Invalid ticket type ID", result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    @DisplayName("Should throw exception if ticket type ID is invalid in POST /create")
    void createTicket_shouldThrowWhenTicketTypeMissing() throws Exception {
        UUID invalidTicketTypeId = UUID.randomUUID();

        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(ticketTypeService.getTicketTypeById(invalidTicketTypeId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/tickets/create")
                        .with(csrf())
                        .param("ticketTypeId", invalidTicketTypeId.toString())
                        .param("quota", "2"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException))
                .andExpect(result -> assertEquals("Invalid ticket type ID", result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    @DisplayName("Should return ticket list view with ticket and event data")
    void listUserTickets_shouldReturnViewWithTicketAndEventList() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setAttendee(attendee);
        ticket.setTicketType(sampleType);

        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(ticketService.getTicketsByAttendeeId(attendee.getId())).thenReturn(List.of(ticket));
        when(eventManagementService.getEvent(sampleType.getEventId())).thenReturn(sampleEvent);

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/list"))
                .andExpect(model().attributeExists("ticketWithEventList"));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    @DisplayName("Should delete ticket and return no content")
    void deleteTicket_shouldReturnNoContent() throws Exception {
        UUID ticketId = UUID.randomUUID();

        mockMvc.perform(delete("/tickets/" + ticketId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(ticketService).deleteTicket(ticketId);
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "ATTENDEE")
    @DisplayName("Should update ticket and return updated ticket")
    void updateTicket_shouldReturnUpdatedTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();

        Ticket updated = new Ticket();
        updated.setId(ticketId);
        updated.setAttendee(attendee);
        updated.setConfirmationCode("TKT-NEW");
        updated.setTicketType(sampleType);

        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(ticketService.updateTicket(eq(ticketId), any(Ticket.class))).thenReturn(updated);

        mockMvc.perform(put("/tickets/" + ticketId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "id": "%s",
                      "confirmationCode": "TKT-NEW"
                    }
                    """.formatted(ticketId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.confirmationCode").value("TKT-NEW"));

        verify(ticketService).updateTicket(eq(ticketId), any(Ticket.class));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "USER")
    @DisplayName("Should filter out ticket when TicketType is null")
    void listUserTickets_shouldSkipTicketIfTypeIsNull() throws Exception {
        Ticket ticketWithoutType = new Ticket();
        ticketWithoutType.setAttendee(attendee);
        ticketWithoutType.setTicketType(null);

        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(ticketService.getTicketsByAttendeeId(attendee.getId())).thenReturn(List.of(ticketWithoutType));

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/list"))
                .andExpect(model().attribute("ticketWithEventList", List.of())); // list empty
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "USER")
    @DisplayName("Should filter out ticket when event is null")
    void listUserTickets_shouldSkipTicketIfEventIsNull() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setAttendee(attendee);
        ticket.setTicketType(sampleType);

        when(userService.getUserByEmail("attendee@example.com")).thenReturn(attendee);
        when(ticketService.getTicketsByAttendeeId(attendee.getId())).thenReturn(List.of(ticket));
        when(eventManagementService.getEvent(sampleType.getEventId())).thenReturn(null); // event empty

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket/list"))
                .andExpect(model().attribute("ticketWithEventList", List.of()));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = "USER")
    @DisplayName("Should return 404 Not Found when ticket with confirmation code does not exist")
    void getByConfirmationCode_shouldReturnNotFoundIfTicketMissing() throws Exception {
        String missingCode = "TKT-MISSING";

        when(ticketService.getTicketByConfirmationCode(missingCode))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/tickets/code/" + missingCode))
                .andExpect(status().isNotFound());
    }
}