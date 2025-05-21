package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Model model;

    @Mock
    private Principal principal;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ProfileController profileController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhoneNumber("1234567890");
        testUser.setPassword("hashedpassword");
    }

    @Test
    void profilePage_shouldReturnProfileViewWithUserData() {
        // Arrange
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);

        // Act
        String viewName = profileController.profilePage(model, principal);

        // Assert
        assertEquals("profile", viewName);
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void updateProfile_shouldUpdateUserAndRedirect() {
        // Arrange
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);

        // Act
        String viewName = profileController.updateProfile(
                "Updated Name", "updated@example.com", "9876543210", principal, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", viewName);
        verify(userService).updateUser(1, "Updated Name", "updated@example.com", "9876543210");
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }

    @Test
    void changePassword_shouldFailWithIncorrectCurrentPassword() {
        // Arrange
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", "hashedpassword")).thenReturn(false);

        // Act
        String viewName = profileController.changePassword(
                "wrongpassword", "newpassword", "newpassword", principal, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), eq("Current password is incorrect"));
    }

    @Test
    void changePassword_shouldFailWithMismatchedNewPasswords() {
        // Arrange
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(passwordEncoder.matches("currentpassword", "hashedpassword")).thenReturn(true);

        // Act
        String viewName = profileController.changePassword(
                "currentpassword", "newpassword", "differentpassword", principal, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), eq("New passwords do not match"));
    }

    @Test
    void changePassword_shouldSucceedWithCorrectPasswords() {
        // Arrange
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(passwordEncoder.matches("currentpassword", "hashedpassword")).thenReturn(true);

        // Act
        String viewName = profileController.changePassword(
                "currentpassword", "newpassword", "newpassword", principal, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", viewName);
        verify(userService).updateUserPassword(1, "newpassword");
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }
}