package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MainControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private Principal principal;

    @InjectMocks
    private MainController mainController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
    }

    @Test
    void dashboard_whenUserIsNotAuthenticated_shouldReturnDashboardViewWithGuestData() {
        // Act
        String viewName = mainController.dashboard(model, null);

        // Assert
        assertEquals("dashboard", viewName);
        verify(model).addAttribute(eq("user"), any(User.class));
        verify(model).addAttribute("isGuest", true);
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
}