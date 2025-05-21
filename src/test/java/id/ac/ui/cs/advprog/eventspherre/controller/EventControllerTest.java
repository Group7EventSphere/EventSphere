package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventManagementService eventManager;

    private Map<String, Object> eventDetails;

    @BeforeEach
    void setUp() {
        eventDetails = new HashMap<>();
        eventDetails.put("title", "Test Event");
        eventDetails.put("description", "Test Description");
        eventDetails.put("eventDate", "2024-12-31");
        eventDetails.put("location", "Jakarta");
        eventDetails.put("organizerId", 1);
        eventManager.clearAllEvents();
    }

    @Test
    void testCreateEvent() throws Exception {
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Event\",\"description\":\"Test Description\",\"eventDate\":\"2024-12-31\",\"location\":\"Jakarta\",\"organizerId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.eventDate").value("2024-12-31"))
                .andExpect(jsonPath("$.location").value("Jakarta"))
                .andExpect(jsonPath("$.organizerId").value(1));
    }

    @Test
    void testGetEvent() throws Exception {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1);

        mockMvc.perform(get("/api/events/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.title").value("Test Event"));
    }

    @Test
    void testGetAllEvents() throws Exception {
        eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testDeleteEvent() throws Exception {
        Event event = eventManager.createEvent(
                "Test Event", "Test Description", "2024-12-31", "Jakarta", 1);

        mockMvc.perform(delete("/api/events/" + event.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/" + event.getId()))
                .andExpect(status().isNotFound());
    }
}