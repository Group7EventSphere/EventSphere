package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.service.EventManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class EventControllerTest {

    @Autowired
    private EventController eventController;

    @Autowired
    private EventManager eventManager;

    private MockMvc mockMvc;
    private Map<String, Object> eventDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        eventDetails = new HashMap<>();
        eventDetails.put("title", "Test Event");
        eventDetails.put("date", "2024-12-31");
        eventManager.clearAllEvents(); // Ensure the EventManager is cleared before each test
    }

    @Test
    void testCreateEvent() throws Exception {
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Event\",\"date\":\"2024-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.title").value("Test Event"));
    }

    @Test
    void testGetEvent() throws Exception {
        Event event = eventManager.createEvent(eventDetails);

        mockMvc.perform(get("/api/events/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId()))
                .andExpect(jsonPath("$.details.title").value("Test Event"));
    }

    @Test
void testGetAllEvents() throws Exception {
    eventManager.createEvent(eventDetails);

    mockMvc.perform(get("/api/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1));
}

    @Test
    void testDeleteEvent() throws Exception {
        Event event = eventManager.createEvent(eventDetails);

        mockMvc.perform(delete("/api/events/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Event deleted successfully"));

        mockMvc.perform(get("/api/events/" + event.getId()))
                .andExpect(status().isNotFound());
    }
}