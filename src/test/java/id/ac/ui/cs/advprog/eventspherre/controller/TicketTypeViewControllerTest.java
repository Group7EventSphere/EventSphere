package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.WebSecurityTestConfig;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketTypeViewController.class)
@Import(WebSecurityTestConfig.class)
@ActiveProfiles("test")
@WithMockUser(username = "admin@example.com", roles = "ORGANIZER")
public class TicketTypeViewControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    TicketTypeService ticketTypeService;

    @MockBean
    UserService userService;

    @MockBean
    EventManagementService eventManagementService;

    @MockBean
    AuthenticationProvider authenticationProvider;

    private User mockUser;
    private Event sampleEvent;
    private TicketType standard;
    private UUID typeId;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("admin@example.com");
        mockUser.setRole(User.Role.ORGANIZER);

        sampleEvent = new Event();
        sampleEvent.setId(1);
        sampleEvent.setTitle("Sample Event");

        typeId = UUID.randomUUID();
        standard = new TicketType("Standard", new BigDecimal("50.00"), 100);
        standard.setId(typeId);
        standard.setEventId(1);
    }

    @Test
    @DisplayName("GET /events/{eventId}/ticket-types - should show manage view when event exists")
    void listTicketTypes_shouldShowManageView_whenEventExists() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(sampleEvent);
        when(ticketTypeService.findByEventId(1)).thenReturn(List.of(standard));

        mockMvc.perform(get("/events/1/ticket-types"))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket-type/manage"))
                .andExpect(model().attribute("event", sampleEvent))
                .andExpect(model().attribute("ticketTypes", List.of(standard)));
    }

    @Test
    @DisplayName("GET /events/{eventId}/ticket-types - should redirect if event is null")
    void listTicketTypes_shouldRedirect_whenEventNull() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(null);

        mockMvc.perform(get("/events/1/ticket-types"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/view"));
    }

    @Test
    @DisplayName("GET /events/{eventId}/ticket-types/create - should show form if event exists")
    void showCreateForm_shouldShowForm_whenEventExists() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(sampleEvent);
        when(userService.getUserByEmail(mockUser.getEmail())).thenReturn(mockUser);

        mockMvc.perform(get("/events/1/ticket-types/create").principal(() -> mockUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket-type/type_form"))
                .andExpect(model().attribute("event", sampleEvent))
                .andExpect(model().attribute("isOrganizer", Boolean.TRUE));
    }

    @Test
    @DisplayName("GET /events/{eventId}/ticket-types/create - should redirect if event is null")
    void showCreateForm_shouldRedirect_whenEventNull() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(null);

        mockMvc.perform(get("/events/1/ticket-types/create").principal(() -> mockUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/view"));
    }

    @Test
    @DisplayName("POST /events/{eventId}/ticket-types/create - should create and redirect")
    void createTicketType_shouldCreateAndRedirect() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(sampleEvent);
        when(userService.getUserByEmail(mockUser.getEmail())).thenReturn(mockUser);
        when(ticketTypeService.create(any(), any(), anyInt(), eq(mockUser), eq(1))).thenReturn(standard);

        mockMvc.perform(post("/events/1/ticket-types/create")
                        .with(csrf())
                        .principal(() -> mockUser.getEmail())
                        .param("name", "Standard")
                        .param("price", "50.00")
                        .param("quota", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/1/ticket-types"));

        verify(ticketTypeService).associateWithEvent(eq(typeId), eq(1));
    }

    @Test
    @DisplayName("POST /events/{eventId}/ticket-types/create - should redirect if event is null")
    void createTicketType_shouldRedirect_whenEventNull() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(null);

        mockMvc.perform(post("/events/1/ticket-types/create")
                        .with(csrf())
                        .principal(() -> mockUser.getEmail())
                        .param("name", "Standard")
                        .param("price", "50.00")
                        .param("quota", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/view"));
    }

    @Test
    @DisplayName("GET /events/{eventId}/ticket-types/edit/{id} - should show edit form")
    void showEditForm_shouldShowForm_whenFound() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(sampleEvent);
        when(userService.getUserByEmail(mockUser.getEmail())).thenReturn(mockUser);
        when(ticketTypeService.getTicketTypeById(typeId)).thenReturn(Optional.of(standard));

        mockMvc.perform(get("/events/1/ticket-types/edit/" + typeId).principal(() -> mockUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket-type/type_edit"))
                .andExpect(model().attribute("event", sampleEvent))
                .andExpect(model().attribute("ticketType", standard))
                .andExpect(model().attribute("isOrganizer", Boolean.TRUE));
    }

    @Test
    @DisplayName("GET /events/{eventId}/ticket-types/edit/{id} - should redirect if event null")
    void showEditForm_shouldRedirect_whenEventNull() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(null);

        mockMvc.perform(get("/events/1/ticket-types/edit/" + typeId).principal(() -> mockUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/view"));
    }

    @Test
    @DisplayName("GET /events/{eventId}/ticket-types/edit/{id} - should throw when ticket type not found")
    void showEditForm_shouldThrow_whenTicketNotFound() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(sampleEvent);
        when(ticketTypeService.getTicketTypeById(typeId)).thenReturn(Optional.empty());
        when(userService.getUserByEmail(mockUser.getEmail())).thenReturn(mockUser);

        mockMvc.perform(get("/events/1/ticket-types/edit/" + typeId).principal(() -> mockUser.getEmail()))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException))
                .andExpect(result -> assertEquals("TicketType not found", result.getResolvedException().getMessage()));
    }

    @Test
    @DisplayName("POST /events/{eventId}/ticket-types/edit/{id} - should update and redirect")
    void updateTicketType_shouldUpdateAndRedirect() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(sampleEvent);
        when(userService.getUserByEmail(mockUser.getEmail())).thenReturn(mockUser);

        mockMvc.perform(post("/events/1/ticket-types/edit/" + typeId)
                        .with(csrf())
                        .principal(() -> mockUser.getEmail())
                        .param("name", "Standard")
                        .param("price", "50.00")
                        .param("quota", "100")
                        .param("eventId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/1/ticket-types"));

        verify(ticketTypeService).updateTicketType(eq(typeId), any(TicketType.class), eq(mockUser));
    }

    @Test
    @DisplayName("POST /events/{eventId}/ticket-types/edit/{id} - should redirect if event null")
    void updateTicketType_shouldRedirect_whenEventNull() throws Exception {
        when(eventManagementService.getEvent(1)).thenReturn(null);

        mockMvc.perform(post("/events/1/ticket-types/edit/" + typeId)
                        .with(csrf())
                        .principal(() -> mockUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/view"));
    }

    @Test
    @DisplayName("POST /events/{eventId}/ticket-types/delete/{id} - should delete and redirect with success message")
    void deleteTicketType_shouldDeleteAndRedirectWithSuccess() throws Exception {
        when(userService.getUserByEmail(mockUser.getEmail())).thenReturn(mockUser);

        mockMvc.perform(post("/events/1/ticket-types/delete/" + typeId)
                        .with(csrf())
                        .principal(() -> mockUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/1/ticket-types"))
                .andExpect(flash().attribute("successMessage", "Ticket type deleted successfully."));
    }

    @Test
    @DisplayName("POST /events/{eventId}/ticket-types/delete/{id} - should redirect with error message on failure")
    void deleteTicketType_shouldRedirectWithError_whenExceptionThrown() throws Exception {
        when(userService.getUserByEmail(mockUser.getEmail())).thenReturn(mockUser);
        doThrow(new IllegalStateException("Cannot delete")).when(ticketTypeService).deleteTicketType(eq(typeId), eq(mockUser));

        mockMvc.perform(post("/events/1/ticket-types/delete/" + typeId)
                        .with(csrf())
                        .principal(() -> mockUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/1/ticket-types"))
                .andExpect(flash().attribute("errorMessage", "Cannot delete"));
    }
}
