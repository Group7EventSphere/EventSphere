package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.WebSecurityTestConfig;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@WebMvcTest(EventController.class)
@Import(WebSecurityTestConfig.class)
@ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventManagementService eventManagementService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TicketTypeService ticketTypeService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;    private User mockOrganizer;
    private Event mockEvent;

    @BeforeEach
    void setUp() {
        // a sample organizer
        mockOrganizer = new User();
        mockOrganizer.setId(1);
        mockOrganizer.setEmail("organizer@example.com");
        mockOrganizer.setName("Test Organizer");
        mockOrganizer.setRole(User.Role.ORGANIZER);

        // a sample event
        mockEvent = new Event();
        mockEvent.setId(1);
        mockEvent.setTitle("Test Event");
        mockEvent.setDescription("Test Description");
        mockEvent.setEventDate("2024-12-31");        mockEvent.setLocation("Jakarta");
        mockEvent.setOrganizerId(1);
    }    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void deleteEvent_shouldRedirectAfterDeletion() throws Exception {
        Event localMockEvent = new Event();
        localMockEvent.setId(1);
        localMockEvent.setTitle("Test Event");
        localMockEvent.setOrganizerId(1); // Same as mockOrganizer.getId()

        when(eventManagementService.getEventById(1)).thenReturn(localMockEvent);
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
        mockAdmin.setRole(User.Role.ADMIN);        Event localMockEvent = new Event();
        localMockEvent.setId(1);
        localMockEvent.setTitle("Test Event");
        localMockEvent.setOrganizerId(1); // Different from admin's ID

        when(eventManagementService.getEventById(1)).thenReturn(localMockEvent);
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
        otherOrganizer.setName("Other Organizer");        otherOrganizer.setRole(User.Role.ORGANIZER);

        Event localMockEvent = new Event();
        localMockEvent.setId(1);
        localMockEvent.setTitle("Test Event");
        localMockEvent.setOrganizerId(1); // mockOrganizer's ID, not otherOrganizer's

        when(eventManagementService.getEventById(1)).thenReturn(localMockEvent);
        when(userService.getUserByEmail("other-organizer@example.com")).thenReturn(otherOrganizer);

        mockMvc.perform(post("/events/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/1")) // Changed: returns to event details for unauthorized access
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
            .andExpect(redirectedUrl("/events")) // returns to events list for user not found
            .andExpect(flash().attributeExists("errorMessage"))
            .andExpect(flash().attribute("errorMessage", "Event not found."));
}

    @Test
    @WithMockUser(username = "user@example.com", roles = {"ATTENDEE"})
    void showEventDetails_withInvalidId_shouldHandleError() throws Exception {
        when(eventManagementService.getEventById(999)).thenReturn(null);

        mockMvc.perform(get("/events/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void showEditEventForm_shouldReturnView() throws Exception {
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);

        mockMvc.perform(get("/events/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/edit"))
                .andExpect(model().attributeExists("eventForm"));
    }

    @Test
    @WithMockUser(username = "other-organizer@example.com", roles = {"ORGANIZER"})
    void showEditEventForm_notOwner_shouldDenyAccess() throws Exception {
        User otherOrganizer = new User();
        otherOrganizer.setId(3);
        otherOrganizer.setEmail("other-organizer@example.com");
        otherOrganizer.setRole(User.Role.ORGANIZER);

        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        when(userService.getUserByEmail("other-organizer@example.com")).thenReturn(otherOrganizer);

        mockMvc.perform(get("/events/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void updateEvent_shouldRedirectAfterUpdate() throws Exception {
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);        when(eventManagementService.updateEvent(
                1,
                "Updated Event",
                "Updated Description",
                "2024-12-31",
                "Updated Location",
                200,
                false
        )).thenReturn(mockEvent);

        mockMvc.perform(post("/events/1/edit")
                        .with(csrf())
                        .param("title", "Updated Event")
                        .param("description", "Updated Description")
                        .param("eventDate", "2024-12-31")
                        .param("location", "Updated Location")
                        .param("capacity", "200")
                        .param("public", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("successMessage"));
    }    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void createEvent_withValidationErrors_shouldReturnFormWithErrors() throws Exception {
        // Simulate validation error by omitting required fields
        mockMvc.perform(post("/events/create")
                .with(csrf())
                .param("title", "") // Empty title triggers validation error
                .param("description", "Test Description")
                .param("eventDate", "2024-12-31")
                .param("location", "Jakarta")
                .param("capacity", "100")
                .param("public", "true"))
            .andExpect(status().isOk())
            .andExpect(view().name("events/create"))
            .andExpect(model().attributeHasErrors("eventForm"))
            .andExpect(model().attributeHasFieldErrors("eventForm", "title"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void toggleEventVisibility_shouldToggleAndRedirect() throws Exception {
        User mockAdmin = new User();
        mockAdmin.setId(2);
        mockAdmin.setEmail("admin@example.com");
        mockAdmin.setRole(User.Role.ADMIN);

        when(userService.getUserByEmail("admin@example.com")).thenReturn(mockAdmin);
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);

        mockMvc.perform(post("/events/1/toggle-visibility")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void showEditEventForm_eventNotFound_shouldRedirect() throws Exception {
        when(eventManagementService.getEventById(999)).thenReturn(null);
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);

        mockMvc.perform(get("/events/999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Event not found."));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void showEditEventForm_asAdmin_shouldReturnView() throws Exception {
        User mockAdmin = new User();
        mockAdmin.setId(2);
        mockAdmin.setEmail("admin@example.com");
        mockAdmin.setRole(User.Role.ADMIN);

        when(eventManagementService.getEventById(1)).thenReturn(mockEvent); // mockEvent is organized by mockOrganizer (ID 1)
        when(userService.getUserByEmail("admin@example.com")).thenReturn(mockAdmin);

        mockMvc.perform(get("/events/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/edit"))
                .andExpect(model().attributeExists("eventForm"))
                .andExpect(model().attribute("eventId", 1));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void showEditEventForm_serviceException_shouldRedirect() throws Exception {
        when(eventManagementService.getEventById(1)).thenThrow(new RuntimeException("Service error"));
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);

        mockMvc.perform(get("/events/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Failed to load event."));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void updateEvent_serviceException_shouldRedirect() throws Exception {
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);
        // No need to mock getEventById for this specific exception test on update
        doThrow(new RuntimeException("Update failed")).when(eventManagementService).updateEvent(
                eq(1),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyInt(),
                anyBoolean()
        );

        mockMvc.perform(post("/events/1/edit")
                        .with(csrf())
                        .param("title", "Updated Event")
                        .param("description", "Updated Description")
                        .param("eventDate", "2024-12-31")
                        .param("location", "Updated Location")
                        .param("capacity", "200")
                        .param("public", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Failed to update event: Update failed"));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = {"ATTENDEE"})
    void createEvent_byAttendee_shouldDenyAccess() throws Exception {
        User mockAttendee = new User();
        mockAttendee.setId(3);
        mockAttendee.setEmail("attendee@example.com");
        mockAttendee.setRole(User.Role.ATTENDEE);

        when(userService.getUserByEmail("attendee@example.com")).thenReturn(mockAttendee);

        mockMvc.perform(post("/events/create")
                        .with(csrf())
                        .param("title", "Attendee Event")
                        .param("description", "Attempt by attendee")
                        .param("eventDate", "2024-01-01")
                        .param("location", "Test Location")
                        .param("capacity", "50")
                        .param("public", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/create"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "You are not authorized to create events."));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void createEvent_asAdmin_shouldRedirectAfterCreation() throws Exception {
        User mockAdmin = new User();
        mockAdmin.setId(2); // Different ID from mockOrganizer
        mockAdmin.setEmail("admin@example.com");
        mockAdmin.setRole(User.Role.ADMIN);

        when(userService.getUserByEmail("admin@example.com")).thenReturn(mockAdmin);        when(eventManagementService.createEvent(
                "Admin Event",
                "Created by Admin",
                "2025-01-01",
                "Admin Location",
                mockAdmin.getId(),
                150,
                false
        )).thenReturn(new Event()); // Return a new event or a mock

        mockMvc.perform(post("/events/create")
                        .with(csrf())
                        .param("title", "Admin Event")
                        .param("description", "Created by Admin")
                        .param("eventDate", "2025-01-01")
                        .param("location", "Admin Location")
                        .param("capacity", "150")
                        .param("public", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void createEvent_serviceException_shouldRedirect() throws Exception {
        // Mock the user service to return the organizer
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);

        // Mock the event management service to throw an exception
        when(eventManagementService.createEvent(
                anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean()
        )).thenThrow(new RuntimeException("Creation failed"));

        // Test with a valid principal
        mockMvc.perform(post("/events/create")
                        .with(csrf())
                        .param("title", "Exception Event")
                        .param("description", "Test Description")
                        .param("eventDate", "2024-12-31")
                        .param("location", "Jakarta")
                        .param("capacity", "100")
                        .param("public", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/create"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Could not create event: Creation failed"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"ATTENDEE"})
    void showEventDetails_serviceException_shouldRedirect() throws Exception {
        when(eventManagementService.getEventById(1)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/events/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Could not load event details."));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void deleteEvent_eventNotFound_shouldRedirect() throws Exception {
        // When getEventById returns null (event not found)
        when(eventManagementService.getEventById(999)).thenReturn(null);
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);

        mockMvc.perform(post("/events/999/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events")) // Changed: returns to events list for event not found
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Event not found."));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void deleteEvent_serviceException_shouldRedirect() throws Exception {
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);
        doThrow(new RuntimeException("Deletion failed")).when(eventManagementService).deleteEvent(1);

        mockMvc.perform(post("/events/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Failed to delete event: Deletion failed"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteEvent_userNotFound_shouldRedirect() throws Exception {
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        when(userService.getUserByEmail("admin@example.com")).thenReturn(null);

        mockMvc.perform(post("/events/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login")) // Changed: returns to login for user not found
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "User not found."));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void toggleEventVisibility_eventNotFound_shouldRedirect() throws Exception {
        when(eventManagementService.getEventById(999)).thenReturn(null);

        mockMvc.perform(post("/events/999/toggle-visibility")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events")) // Changed: returns to events list for event not found
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Event not found."));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void toggleEventVisibility_serviceException_shouldRedirect() throws Exception {
        mockEvent.setPublic(true);
        User mockAdmin = new User();
        mockAdmin.setId(2);
        mockAdmin.setEmail("admin@example.com");
        mockAdmin.setRole(User.Role.ADMIN);

        when(userService.getUserByEmail("admin@example.com")).thenReturn(mockAdmin);
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);        
        // The controller will toggle to false (private) since mockEvent.isPublic() is true
        doThrow(new RuntimeException("Toggle failed")).when(eventManagementService).updateEvent(
                1,
                mockEvent.getTitle(),
                mockEvent.getDescription(),
                mockEvent.getEventDate(),
                mockEvent.getLocation(),
                mockEvent.getCapacity(),
                false
        );

        mockMvc.perform(post("/events/1/toggle-visibility")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/1")) // Changed: returns to event details for general errors
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Failed to toggle event visibility: Toggle failed"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void toggleEventVisibility_toPrivate_shouldToggleAndRedirect() throws Exception {
        mockEvent.setPublic(true);
        User mockAdmin = new User();
        mockAdmin.setId(2);
        mockAdmin.setEmail("admin@example.com");
        mockAdmin.setRole(User.Role.ADMIN);

        when(userService.getUserByEmail("admin@example.com")).thenReturn(mockAdmin);
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        when(eventManagementService.updateEvent(1, mockEvent.getTitle(), mockEvent.getDescription(), mockEvent.getEventDate(), mockEvent.getLocation(), mockEvent.getCapacity(), false))
                .thenReturn(mockEvent);

        mockMvc.perform(post("/events/1/toggle-visibility")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "Event visibility changed to private."));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void toggleEventVisibility_toPublic_shouldToggleAndRedirect() throws Exception {
        mockEvent.setPublic(false);
        User mockAdmin = new User();
        mockAdmin.setId(2);
        mockAdmin.setEmail("admin@example.com");
        mockAdmin.setRole(User.Role.ADMIN);

        when(userService.getUserByEmail("admin@example.com")).thenReturn(mockAdmin);
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        when(eventManagementService.updateEvent(1, mockEvent.getTitle(), mockEvent.getDescription(), mockEvent.getEventDate(), mockEvent.getLocation(), mockEvent.getCapacity(), true))
                .thenReturn(mockEvent);

        mockMvc.perform(post("/events/1/toggle-visibility")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "Event visibility changed to public."));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"ATTENDEE"})
    void listEvents_shouldReturnView() throws Exception {
        List<Event> events = List.of(mockEvent);
        when(eventManagementService.getAllEvents()).thenReturn(events);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/list"))
                .andExpect(model().attributeExists("events"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"ATTENDEE"})
    void listEvents_shouldHandleException() throws Exception {
        when(eventManagementService.getAllEvents()).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/list"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Could not load events."));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"ATTENDEE"})
    void listEvents_withNullEvents_shouldHandleGracefully() throws Exception {
        // Test the branch where getAllEvents returns null
        when(eventManagementService.getAllEvents()).thenReturn(null);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/list"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("events", org.hamcrest.Matchers.instanceOf(ArrayList.class)))
                .andExpect(model().attribute("events", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void manageEvents_shouldReturnView() throws Exception {
        when(eventManagementService.getAllEvents())
                .thenReturn(List.of(mockEvent));
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
    }    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void createEvent_shouldRedirectAfterCreation() throws Exception {
        when(userService.getUserByEmail("organizer@example.com"))
                .thenReturn(mockOrganizer);        when(eventManagementService.createEvent(
                "Test Event",
                "Test Description",
                "2024-12-31",
                "Jakarta",
                mockOrganizer.getId(),
                100,
                true
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
        when(userService.getUserByEmail("attendee@example.com"))
                .thenReturn(null);

        mockMvc.perform(post("/events/create")
                        .with(csrf())
                        .param("title", "Test Event"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/create"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage",
                        containsString("Cannot invoke")));

    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"ATTENDEE"})
    void showEventDetails_shouldReturnView() throws Exception {
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/detail"))
                .andExpect(model().attributeExists("event"));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void toggleEventVisibility_accessDeniedForOrganizer() throws Exception {
        // Setup mock objects
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        when(userService.getUserByEmail("organizer@example.com")).thenReturn(mockOrganizer);

        mockMvc.perform(post("/events/1/toggle-visibility")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/manage"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "Event visibility changed to public."));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"ATTENDEE"})
    void showEventDetails_withNullTicketTypes_shouldHandleGracefully() throws Exception {
        when(eventManagementService.getEventById(1)).thenReturn(mockEvent);
        // Return null for ticket types to test null-safety
        when(ticketTypeService.getTicketTypesByEventId(1)).thenReturn(null);

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/detail"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("ticketTypes"))
                .andExpect(model().attribute("ticketTypes", org.hamcrest.Matchers.instanceOf(ArrayList.class)))
                .andExpect(model().attribute("ticketTypes", org.hamcrest.Matchers.hasSize(0)));
    }
}