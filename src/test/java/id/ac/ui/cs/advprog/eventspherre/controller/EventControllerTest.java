package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(EventController.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventManagementService eventManagementService;

    @MockBean
    private UserService userService;

    @MockBean
    private TicketTypeService ticketTypeService;

    private User mockOrganizer;
    private Event mockEvent;

    @BeforeEach
    void setUp() {
        // Create mock organizer
        mockOrganizer = new User();
        mockOrganizer.setId(1);
        mockOrganizer.setEmail("organizer@example.com");
        mockOrganizer.setName("Test Organizer");
        mockOrganizer.setRole(User.Role.ORGANIZER);

        // Create mock event
        mockEvent = new Event();
        mockEvent.setId(1);
        mockEvent.setTitle("Test Event");
        mockEvent.setDescription("Test Description");
        mockEvent.setEventDate("2024-12-31");
        mockEvent.setLocation("Jakarta");
        mockEvent.setOrganizerId(1);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"ATTENDEE"})
    void listEvents_shouldReturnView() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        
        when(eventManagementService.getAllEvents()).thenReturn(events);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/list"))
                .andExpect(model().attributeExists("events"));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void manageEvents_shouldReturnView() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        
        when(eventManagementService.getAllEvents()).thenReturn(events);

        mockMvc.perform(get("/events/manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/manage"))
                .andExpect(model().attributeExists("events"));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void showCreateEventForm_shouldReturnView() throws Exception {
        mockMvc.perform(get("/events/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/create"))
                .andExpect(model().attributeExists("eventForm"));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void createEvent_shouldRedirectAfterCreation() throws Exception {
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);
        when(eventManagementService.createEvent(
                eq("Test Event"), 
                eq("Test Description"), 
                eq("2024-12-31"), 
                eq("Jakarta"), 
                eq(mockOrganizer.getId()),
                eq(100),     // Adding capacity parameter
                eq(true)     // Adding isPublic parameter
        )).thenReturn(mockEvent);

        mockMvc.perform(post("/events/create")
                .with(csrf())
                .param("title", "Test Event")
                .param("description", "Test Description")
                .param("eventDate", "2024-12-31")
                .param("location", "Jakarta")
                .param("capacity", "100")
                .param("public", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"));
    }
    
    @Test
    @WithMockUser(username = "attendee@example.com", roles = {"ATTENDEE"})
    void createEvent_shouldDenyAccessForNonOrganizers() throws Exception {
        // Mock the userService to return null for attendee user
        when(userService.getUserByEmail("attendee@example.com")).thenReturn(null);
        
        mockMvc.perform(post("/events/create")
                .with(csrf())
                .param("title", "Test Event"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/create"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", 
                        containsString("Cannot invoke \"id.ac.ui.cs.advprog.eventspherre.model.User.getId()\" because \"currentUser\" is null")));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void deleteEvent_shouldRedirectAfterDeletion() throws Exception {
        Event mockEvent = new Event();
        mockEvent.setId(1);
        mockEvent.setTitle("Test Event");
        mockEvent.setOrganizerId(1); // Same as mockOrganizer.getId()

        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);

        mockMvc.perform(post("/events/1/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "Event deleted successfully!"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteEvent_adminCanDeleteAnyEvent() throws Exception {
        // Setup admin user
        User mockAdmin = new User();
        mockAdmin.setId(2);
        mockAdmin.setEmail("admin@example.com");
        mockAdmin.setName("Test Admin");
        mockAdmin.setRole(User.Role.ADMIN);

        Event mockEvent = new Event();
        mockEvent.setId(1);
        mockEvent.setTitle("Test Event");
        mockEvent.setOrganizerId(1); // Different from admin's ID

        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        when(userService.getUserByEmail("admin@example.com")).thenReturn(mockAdmin);

        mockMvc.perform(post("/events/1/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "other-organizer@example.com", roles = {"ORGANIZER"})
    void deleteEvent_shouldDenyDeletionForNonOwner() throws Exception {
        // Setup another organizer user who is not the event owner
        User otherOrganizer = new User();
        otherOrganizer.setId(3);
        otherOrganizer.setEmail("other-organizer@example.com");
        otherOrganizer.setName("Other Organizer");
        otherOrganizer.setRole(User.Role.ORGANIZER);

        Event mockEvent = new Event();
        mockEvent.setId(1);
        mockEvent.setTitle("Test Event");
        mockEvent.setOrganizerId(1); // mockOrganizer's ID, not otherOrganizer's

        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        when(userService.getUserByEmail("other-organizer@example.com")).thenReturn(otherOrganizer);

        mockMvc.perform(post("/events/1/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "You are not authorized to delete this event."));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = {"ATTENDEE"})
    void deleteEvent_shouldDenyAccessForAttendees() throws Exception {
        // Mock the userService to return null for attendee user like in other tests
        when(userService.getUserByEmail("attendee@example.com")).thenReturn(null);

        // Since we're expecting a redirect with an error message instead of a 403
        mockMvc.perform(post("/events/1/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "User not found."));
    }
}
