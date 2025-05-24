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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketTypeController.class)
@Import(WebSecurityTestConfig.class)
@ActiveProfiles("test")
@WithMockUser(username = "admin@example.com", roles = "ORGANIZER")
class TicketTypeControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean TicketTypeService      ticketTypeService;
    @MockBean UserService            userService;
    @MockBean EventManagementService eventManagementService;
    @MockBean AuthenticationProvider authenticationProvider;

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
    @DisplayName("GET /ticket-types should return list view")
    void listTicketTypes() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(mockUser);
        when(eventManagementService.getAllEvents()).thenReturn(List.of(sampleEvent));
        when(ticketTypeService.findAll()).thenReturn(List.of(standard));

        mockMvc.perform(get("/ticket-types"))
               .andExpect(status().isOk())
               .andExpect(view().name("ticket-type/type_list"))
               .andExpect(model().attributeExists("eventTicketList"));
    }

    @Test
    @DisplayName("GET /ticket-types/create should show form")
    void showCreateForm() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(mockUser);

        mockMvc.perform(get("/ticket-types/create"))
               .andExpect(status().isOk())
               .andExpect(view().name("ticket-type/type_form"))
               .andExpect(model().attributeExists("events"))
               .andExpect(model().attributeExists("ticketType"));
    }

    @Test
    @DisplayName("POST /ticket-types/create should call service and redirect")
    void createTicketType() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(mockUser);

        mockMvc.perform(post("/ticket-types/create")
                   .with(csrf())
                   .param("name", "VIP")
                   .param("price", "150.00")
                   .param("quota", "20")
                   .param("eventId", "1"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/ticket-types"));
    }

    @Test
    @DisplayName("POST /ticket-types/delete/{id} should delete and redirect")
    void deleteTicketType() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(mockUser);
        doNothing().when(ticketTypeService).deleteTicketType(typeId, mockUser);

        mockMvc.perform(post("/ticket-types/delete/" + typeId)
                   .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/ticket-types"));
    }
}
