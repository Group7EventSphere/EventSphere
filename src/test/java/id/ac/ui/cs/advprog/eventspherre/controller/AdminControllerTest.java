package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.dto.RegisterUserDto;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.AuthenticationService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private Model model;

    @Mock
    private Principal principal;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminController adminController;

    private User adminUser;
    private User regularUser;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        // Set up test users
        adminUser = new User();
        adminUser.setId(1);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(User.Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2);
        regularUser.setName("Regular User");
        regularUser.setEmail("user@example.com");
        regularUser.setRole(User.Role.ATTENDEE);

        userList = new ArrayList<>(Arrays.asList(adminUser, regularUser));
    }

    @Test
    void userManagement_shouldReturnAllUsers_whenNoFiltersProvided() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(userList);
        when(principal.getName()).thenReturn("admin@example.com");

        // Act
        String viewName = adminController.userManagement(model, principal, null, null);

        // Assert
        assertEquals("admin/user-management", viewName);
        verify(userService).getAllUsers();
        verify(model).addAttribute("users", userList);
        verify(model).addAttribute("currentUserEmail", "admin@example.com");
        verify(model).addAttribute("availableRoles", Arrays.asList(User.Role.values()));
    }

    @Test
    void userManagement_shouldFilterByRole_whenRoleProvided() {
        // Arrange
        when(userService.getUsersByRole("ADMIN")).thenReturn(Arrays.asList(adminUser));
        when(principal.getName()).thenReturn("admin@example.com");

        // Act
        String viewName = adminController.userManagement(model, principal, "ADMIN", null);

        // Assert
        assertEquals("admin/user-management", viewName);
        verify(userService).getUsersByRole("ADMIN");
        verify(model).addAttribute("users", Arrays.asList(adminUser));
        verify(model).addAttribute("currentRole", "ADMIN");
    }

    @Test
    void userManagement_shouldSearchUsers_whenSearchTermProvided() {
        // Arrange
        when(userService.searchUsers("admin")).thenReturn(Arrays.asList(adminUser));
        when(principal.getName()).thenReturn("admin@example.com");

        // Act
        String viewName = adminController.userManagement(model, principal, null, "admin");

        // Assert
        assertEquals("admin/user-management", viewName);
        verify(userService).searchUsers("admin");
        verify(model).addAttribute("users", Arrays.asList(adminUser));
        verify(model).addAttribute("currentSearch", "admin");
    }

    @Test
    void userManagement_shouldCombineRoleAndSearch_whenBothProvided() {
        // Arrange
        when(userService.searchUsersByRoleAndTerm("ADMIN", "admin")).thenReturn(Arrays.asList(adminUser));
        when(principal.getName()).thenReturn("admin@example.com");

        // Act
        String viewName = adminController.userManagement(model, principal, "ADMIN", "admin");

        // Assert
        assertEquals("admin/user-management", viewName);
        verify(userService).searchUsersByRoleAndTerm("ADMIN", "admin");
        verify(model).addAttribute("users", Arrays.asList(adminUser));
        verify(model).addAttribute("currentRole", "ADMIN");
        verify(model).addAttribute("currentSearch", "admin");
    }
    
    @Test
    void userManagement_shouldHandleInvalidRoleWithSearch() {
        // Arrange
        when(userService.searchUsersByRoleAndTerm("INVALID", "admin"))
            .thenThrow(new IllegalArgumentException("Invalid role"));
        when(userService.searchUsers("admin")).thenReturn(Arrays.asList(adminUser));
        when(principal.getName()).thenReturn("admin@example.com");

        // Act
        String viewName = adminController.userManagement(model, principal, "INVALID", "admin");

        // Assert
        assertEquals("admin/user-management", viewName);
        verify(userService).searchUsersByRoleAndTerm("INVALID", "admin");
        verify(userService).searchUsers("admin");
        verify(model).addAttribute("users", Arrays.asList(adminUser));
        verify(model).addAttribute("errorMessage", "Invalid role filter. Showing search results only.");
    }
    
    @Test
    void userManagement_shouldHandleInvalidRoleWithoutSearch() {
        // Arrange
        when(userService.getUsersByRole("INVALID"))
            .thenThrow(new IllegalArgumentException("Invalid role"));
        when(userService.getAllUsers()).thenReturn(userList);
        when(principal.getName()).thenReturn("admin@example.com");

        // Act
        String viewName = adminController.userManagement(model, principal, "INVALID", null);

        // Assert
        assertEquals("admin/user-management", viewName);
        verify(userService).getUsersByRole("INVALID");
        verify(userService).getAllUsers();
        verify(model).addAttribute("users", userList);
        verify(model).addAttribute("errorMessage", "Invalid role filter. Showing all users.");
    }

    @Test
    void updateUser_shouldRedirect_afterSuccessfulUpdate() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.getUserById(2)).thenReturn(regularUser);

        // Act
        String viewName = adminController.updateUser(
                2, "Updated Name", "updated@example.com", "1234567890", 
                "ORGANIZER", principal, redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).updateUser(2, "Updated Name", "updated@example.com", "1234567890");
        verify(userService).updateUserRole(2, "ORGANIZER");
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }
    
    @Test
    void updateUser_shouldAllowAttendeeRole() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.getUserById(2)).thenReturn(regularUser);

        // Act
        String viewName = adminController.updateUser(
                2, "Updated Name", "updated@example.com", "1234567890", 
                "ATTENDEE", principal, redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).updateUser(2, "Updated Name", "updated@example.com", "1234567890");
        // Should update role to ATTENDEE (now supported)
        verify(userService).updateUserRole(2, "ATTENDEE");
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }
    
    @Test
    void updateUser_shouldHandleInvalidRole() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.getUserById(2)).thenReturn(regularUser);
        doThrow(new IllegalArgumentException("Invalid role"))
            .when(userService).updateUserRole(2, "INVALID_ROLE");

        // Act
        String viewName = adminController.updateUser(
                2, "Updated Name", "updated@example.com", "1234567890", 
                "INVALID_ROLE", principal, redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).updateUser(2, "Updated Name", "updated@example.com", "1234567890");
        // Should attempt to update role but handle the exception
        verify(userService).updateUserRole(2, "INVALID_ROLE");
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }
    
    @Test
    void updateUser_shouldUpdateWithoutRole() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");

        // Act
        String viewName = adminController.updateUser(
                2, "Updated Name", "updated@example.com", "1234567890", 
                null, principal, redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).updateUser(2, "Updated Name", "updated@example.com", "1234567890");
        verify(userService, times(0)).updateUserRole(eq(2), anyString());
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }
    
    @Test
    void updateUser_shouldHandleExceptions() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");
        doThrow(new RuntimeException("Update error"))
            .when(userService).updateUser(2, "Updated Name", "updated@example.com", "1234567890");

        // Act
        String viewName = adminController.updateUser(
                2, "Updated Name", "updated@example.com", "1234567890", 
                "ORGANIZER", principal, redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }
    
    @Test
    void updateUser_shouldPreserveSearchAndFilterState() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.getUserById(2)).thenReturn(regularUser);

        // Act
        String viewName = adminController.updateUser(
                2, "Updated Name", "updated@example.com", "1234567890", 
                "ORGANIZER", principal, redirectAttributes, "ADMIN", "search");

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(redirectAttributes).addAttribute("role", "ADMIN");
        verify(redirectAttributes).addAttribute("search", "search");
    }

    @Test
    void updateUser_shouldPreventChangingOwnAdminRole() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.getUserById(1)).thenReturn(adminUser);

        // Act
        String viewName = adminController.updateUser(
                1, "Admin User", "admin@example.com", "1234567890", 
                "ORGANIZER", principal, redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).updateUser(1, "Admin User", "admin@example.com", "1234567890");
        // Should not update role for admin's own account
        verify(userService, times(0)).updateUserRole(1, "ORGANIZER");
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }

    @Test
    void updateUserPassword_shouldRedirectToAdmin_afterSuccessfulUpdate() {
        // Act
        String viewName = adminController.updateUserPassword(1, "newpassword", redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).updateUserPassword(1, "newpassword");
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }
    
    @Test
    void updateUserPassword_shouldHandleExceptions() {
        // Arrange
        doThrow(new RuntimeException("Password update error"))
            .when(userService).updateUserPassword(1, "newpassword");
            
        // Act
        String viewName = adminController.updateUserPassword(1, "newpassword", redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }
    
    @Test
    void updateUserPassword_shouldPreserveSearchAndFilterState() {
        // Act
        String viewName = adminController.updateUserPassword(1, "newpassword", redirectAttributes, "ADMIN", "search");

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(redirectAttributes).addAttribute("role", "ADMIN");
        verify(redirectAttributes).addAttribute("search", "search");
    }

    @Test
    void createUser_shouldRegisterUserAndSetRole() {
        // Arrange
        User newUser = new User();
        newUser.setId(3);
        RegisterUserDto capturedDto = new RegisterUserDto();
        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(newUser);

        // Act
        String viewName = adminController.createUser(
                "New User", "new@example.com", "password123", "1234567890",
                "ADMIN", redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(authenticationService).signup(any(RegisterUserDto.class));
        verify(userService).updateUserRole(3, "ADMIN");
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }
    
    @Test
    void createUser_shouldHandleOrganizerRole() {
        // Arrange
        User newUser = new User();
        newUser.setId(3);
        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(newUser);

        // Act
        String viewName = adminController.createUser(
                "Organizer", "organizer@example.com", "password123", "1234567890",
                "ORGANIZER", redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(authenticationService).signup(any(RegisterUserDto.class));
        verify(userService).updateUserRole(3, "ORGANIZER");
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }
    
    @Test
    void createUser_shouldSetRoleForAttendee() {
        // Arrange
        User newUser = new User();
        newUser.setId(3);
        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(newUser);

        // Act
        String viewName = adminController.createUser(
                "Attendee", "attendee@example.com", "password123", "1234567890",
                "ATTENDEE", redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(authenticationService).signup(any(RegisterUserDto.class));
        // Should set role for ATTENDEE (now supported)
        verify(userService).updateUserRole(3, "ATTENDEE");
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }
    
    @Test
    void createUser_shouldSkipInvalidRole() {
        // Arrange
        User newUser = new User();
        newUser.setId(3);
        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(newUser);

        // Act
        String viewName = adminController.createUser(
                "New User", "new@example.com", "password123", "1234567890",
                "INVALID_ROLE", redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(authenticationService).signup(any(RegisterUserDto.class));
        // Should not attempt to update role for invalid roles
        verify(userService, never()).updateUserRole(anyInt(), anyString());
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }
    
    @Test
    void createUser_shouldHandleExceptions() {
        // Arrange
        doThrow(new RuntimeException("Registration error"))
            .when(authenticationService).signup(any(RegisterUserDto.class));

        // Act
        String viewName = adminController.createUser(
                "New User", "new@example.com", "password123", "1234567890",
                "ADMIN", redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }
    
    @Test
    void createUser_shouldPreserveSearchAndFilterState() {
        // Arrange
        User newUser = new User();
        newUser.setId(3);
        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(newUser);

        // Act
        String viewName = adminController.createUser(
                "New User", "new@example.com", "password123", "1234567890",
                "ADMIN", redirectAttributes, "ADMIN", "search");

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(redirectAttributes).addAttribute("role", "ADMIN");
        verify(redirectAttributes).addAttribute("search", "search");
    }

    @Test
    void deleteUser_shouldPreventDeletingOwnAccount() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.getUserById(1)).thenReturn(adminUser);

        // Act
        String viewName = adminController.deleteUser(1, principal, redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService, times(0)).deleteUser(1);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    void deleteUser_shouldDeleteOtherUserAccount() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.getUserById(2)).thenReturn(regularUser);

        // Act
        String viewName = adminController.deleteUser(2, principal, redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).deleteUser(2);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }
    
    @Test
    void deleteUser_shouldHandleExceptions() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.getUserById(2)).thenReturn(regularUser);
        doThrow(new RuntimeException("Deletion error")).when(userService).deleteUser(2);

        // Act
        String viewName = adminController.deleteUser(2, principal, redirectAttributes, null, null);

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).deleteUser(2);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }
    
    @Test
    void deleteUser_shouldPreserveSearchAndFilterState() {
        // Arrange
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.getUserById(2)).thenReturn(regularUser);

        // Act
        String viewName = adminController.deleteUser(2, principal, redirectAttributes, "ADMIN", "search");

        // Assert
        assertEquals("redirect:/admin/users", viewName);
        verify(redirectAttributes).addAttribute("role", "ADMIN");
        verify(redirectAttributes).addAttribute("search", "search");
    }
}