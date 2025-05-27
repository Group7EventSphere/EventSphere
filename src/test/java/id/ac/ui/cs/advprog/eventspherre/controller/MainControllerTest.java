package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Mock
    private AdService adService;

    @Mock
    private UserService userService;
    
    @Mock
    private EventManagementService eventManagementService;

    @Mock
    private Model model;

    @Mock
    private Principal principal;

    private MainController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock empty list of events by default
        when(eventManagementService.findPublicEvents()).thenReturn(new ArrayList<>());
        controller = new MainController(userService, adService);
    }

    @Test
    void dashboard_whenUserIsNotAuthenticated_shouldShowGuest() {
        // GIVEN: no Principal → unauthenticated guest
        List<Ad> emptyAds = Collections.emptyList();
        when(adService.getAllAds()).thenReturn(emptyAds);

        // WHEN
        String viewName = controller.dashboard(model, /* principal = */ null);

        // THEN
        assertEquals("dashboard", viewName);
        verify(model).addAttribute("user", authenticatedUser);
        verify(model, never()).addAttribute(eq("isGuest"), any());
        verify(model).addAttribute(eq("recentEvents"), any(List.class));
    }

    @Test
    void dashboard_whenUserIsNotAuthenticated_shouldReturnDashboardViewWithGuestData() {
        // Act
        String viewName = mainController.dashboard(model, null);

        // Assert
        assertEquals("dashboard", viewName);
        verify(model).addAttribute(eq("user"), any(User.class));

        // ads must always be on the model
        verify(model).addAttribute("ads", emptyAds);

        // a guest User object is created
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(model).addAttribute(eq("user"), userCaptor.capture());
        User guest = userCaptor.getValue();
        assertEquals("Guest", guest.getName());
        assertEquals("Not logged in", guest.getEmail());
        assertEquals(0.0, guest.getBalance());

        // isGuest flag must be true
        verify(model).addAttribute("isGuest", true);
        verify(model).addAttribute(eq("recentEvents"), any(List.class));

        // userService must NOT be called at all
        verifyNoInteractions(userService);
    }

    @Test
    void dashboard_whenUserIsAuthenticated_shouldShowUser() {
        // GIVEN: a non-null Principal
        when(principal.getName()).thenReturn("bob@example.com");

        // stub adService and userService
        User bob = new User();
        bob.setName("Bob");
        bob.setEmail("bob@example.com");
        bob.setBalance(999.0);

        List<Ad> ads = List.of(
                Ad.builder()
                        .id(1L)
                        .title("Foo")
                        .description("Bar")
                        .imageUrl("baz.jpg")
                        .build()
        );

        when(adService.getAllAds()).thenReturn(ads);
        when(userService.getUserByEmail("bob@example.com")).thenReturn(bob);

        // WHEN
        String viewName = controller.dashboard(model, principal);

        // THEN
        assertEquals("dashboard", viewName);

        // ads must be loaded
        verify(model).addAttribute("ads", ads);

        // userService must be called with the principal's name
        verify(userService).getUserByEmail("bob@example.com");
        // and that User must be put onto the model
        verify(model).addAttribute("user", bob);

        // there should be NO isGuest attribute
        verify(model, never()).addAttribute(eq("isGuest"), any());
    }
    
    @Test
    void dashboard_shouldShowTop5RecentEvents() {
        // Arrange
        List<Event> mockEvents = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Event event = new Event();
            event.setId(i);
            event.setTitle("Event " + i);
            mockEvents.add(event);
        }
        when(eventManagementService.findPublicEvents()).thenReturn(mockEvents);
        
        // Act
        mainController.dashboard(model, null);
        
        // Assert
        verify(model).addAttribute(eq("recentEvents"), argThat(events -> {
            List<Event> eventList = (List<Event>) events;
            return eventList.size() == 5 && 
                   eventList.get(0).getId() == 7 && // Most recent (highest ID)
                   eventList.get(4).getId() == 3;   // 5th most recent
        }));
    }
}