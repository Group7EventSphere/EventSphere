package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MainControllerTest {

    @Mock
    private UserService userService;
    
    @Mock
    private EventManagementService eventManagementService;

    @Mock
    private AdService adService;

    @Mock
    private Model model;

    @Mock
    private Principal principal;

    @InjectMocks
    private MainController mainController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock empty list of events by default
        when(eventManagementService.findPublicEvents()).thenReturn(new ArrayList<>());
        // Mock empty list of ads by default
        when(adService.getAllAds()).thenReturn(new ArrayList<>());
    }

    @Test
    void dashboard_whenUserIsAuthenticated_shouldReturnDashboardViewWithUserData() {
        // Arrange
        User authenticatedUser = new User();
        authenticatedUser.setName("Test User");
        authenticatedUser.setEmail("test@example.com");
        authenticatedUser.setBalance(100.0);

        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(authenticatedUser);

        // Act
        String viewName = mainController.dashboard(model, principal);

        // Assert
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
        verify(model).addAttribute("isGuest", true);
        verify(model).addAttribute(eq("recentEvents"), any(List.class));
    }

    @Test
    void dashboard_whenUserIsNotAuthenticated_shouldCreateGuestUserWithCorrectProperties() {
        // Act
        mainController.dashboard(model, null);

        // Assert
        verify(model).addAttribute(eq("user"), argThat(user -> 
            user instanceof User && 
            "Guest".equals(((User) user).getName()) &&
            "Not logged in".equals(((User) user).getEmail()) &&
            ((User) user).getBalance() == 0.0
        ));
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