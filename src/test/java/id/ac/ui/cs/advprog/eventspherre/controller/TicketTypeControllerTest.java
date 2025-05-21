package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TicketTypeControllerTest {

    private TicketTypeService ticketTypeService;
    private TicketTypeController ticketTypeController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ticketTypeService = mock(TicketTypeService.class);
        ticketTypeController = new TicketTypeController(ticketTypeService);
        mockMvc = MockMvcBuilders.standaloneSetup(ticketTypeController).build();
    }

    @Test
    @DisplayName("GET /ticket-types/create should return the create form")
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/ticket-types/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket-type/create"))
                .andExpect(model().attributeExists("ticketType"));
    }

    @Test
    @DisplayName("POST /ticket-types should save and redirect to list")
    void testCreateTicketType() throws Exception {
        mockMvc.perform(post("/ticket-types")
                        .param("name", "VIP")
                        .param("price", "100.00")
                        .param("quota", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ticket-types"));

        verify(ticketTypeService).create(any(TicketType.class));
    }

    @Test
    @DisplayName("GET /ticket-types should list all ticket types")
    void testListTicketTypes() throws Exception {
        when(ticketTypeService.findAll()).thenReturn(Arrays.asList(
                new TicketType("VIP", new BigDecimal("100.00"), 50),
                new TicketType("Regular", new BigDecimal("50.00"), 100)
        ));

        mockMvc.perform(get("/ticket-types"))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket-type/list"))
                .andExpect(model().attributeExists("ticketTypes"));
    }
}
