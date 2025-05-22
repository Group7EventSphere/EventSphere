package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketTypeController.class)
@WithMockUser(username = "admin@example.com")
class TicketTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketTypeService ticketTypeService;

    @MockBean
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("admin@example.com");
        mockUser.setName("Admin");
    }

    @Test
    @DisplayName("GET /ticket-types should return list view")
    void testListTicketTypes() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(mockUser);
        when(ticketTypeService.findAll()).thenReturn(List.of(
                new TicketType("Standard", new BigDecimal("50.00"), 100),
                new TicketType("VIP", new BigDecimal("150.00"), 10)
        ));

        mockMvc.perform(get("/ticket-types"))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket-type/type_list"))
                .andExpect(model().attributeExists("ticketTypes"));
    }

    @Test
    @DisplayName("GET /ticket-types/create should show form")
    void testShowCreateForm() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(mockUser);

        mockMvc.perform(get("/ticket-types/create").principal(() -> "admin@example.com"))

                .andExpect(status().isOk())
                .andExpect(view().name("ticket-type/type_form"))
                .andExpect(model().attributeExists("ticketType"));
    }

    @Test
    @DisplayName("POST /ticket-types/create should call service with user")
    void testCreateTicketType() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(mockUser);

        mockMvc.perform(post("/ticket-types/create")
                        .with(csrf())
                        .param("name", "Premium")
                        .param("price", "200.00")
                        .param("quota", "20")
                        .principal(() -> "admin@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket-types"));

        verify(ticketTypeService).create(
                eq("Premium"),
                eq(new BigDecimal("200.00")),
                eq(20),
                eq(mockUser)
        );
    }

    @Test
    @DisplayName("POST /ticket-types/delete/{id} with UUID should delete and redirect")
    void testDeleteTicketTypeWithUUID() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(userService.getUserByEmail(anyString())).thenReturn(mockUser);

        mockMvc.perform(post("/ticket-types/delete/" + uuid).with(csrf()).principal(() -> "admin@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket-types"));

        verify(ticketTypeService).deleteTicketType(uuid, mockUser);
    }
}
