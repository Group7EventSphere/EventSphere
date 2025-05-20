package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User organizerUser;
    private User attendeeUser;

    @BeforeEach
    void setUp() {
        // Create sample users for testing
        adminUser = new User();
        adminUser.setId(1);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encoded_admin_password");
        adminUser.setRole(User.Role.ADMIN);

        organizerUser = new User();
        organizerUser.setId(2);
        organizerUser.setName("Event Organizer");
        organizerUser.setEmail("organizer@example.com");
        organizerUser.setPassword("encoded_organizer_password");
        organizerUser.setRole(User.Role.ORGANIZER);

        attendeeUser = new User();
        attendeeUser.setId(3);
        attendeeUser.setName("John Doe");
        attendeeUser.setEmail("john@example.com");
        attendeeUser.setPassword("encoded_attendee_password");
        attendeeUser.setRole(User.Role.ATTENDEE);
    }

    @Test
    void getAllUsersShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(adminUser, organizerUser, attendeeUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(3, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUsersByRoleShouldReturnUsersWithSpecificRole() {
        // Arrange
        List<User> adminUsers = List.of(adminUser);
        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(adminUsers);

        // Act
        List<User> result = userService.getUsersByRole("ADMIN");

        // Assert
        assertEquals(1, result.size());
        assertEquals(User.Role.ADMIN, result.get(0).getRole());
        verify(userRepository, times(1)).findByRole(User.Role.ADMIN);
    }

    @Test
    void getUsersByRoleWithInvalidRoleShouldThrowException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUsersByRole("INVALID_ROLE");
        });
        assertTrue(exception.getMessage().contains("Invalid role"));
    }

    @Test
    void searchUsersShouldReturnMatchingUsers() {
        // Arrange
        List<User> matchingUsers = List.of(adminUser);
        when(userRepository.findByNameOrEmailContainingIgnoreCase("admin")).thenReturn(matchingUsers);

        // Act
        List<User> result = userService.searchUsers("admin");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Admin User", result.get(0).getName());
        verify(userRepository, times(1)).findByNameOrEmailContainingIgnoreCase("admin");
    }

    @Test
    void searchUsersWithEmptyTermShouldReturnAllUsers() {
        // Arrange
        List<User> allUsers = Arrays.asList(adminUser, organizerUser, attendeeUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        // Act
        List<User> result = userService.searchUsers("");

        // Assert
        assertEquals(3, result.size());
        verify(userRepository, times(1)).findAll();
        verify(userRepository, never()).findByNameOrEmailContainingIgnoreCase(anyString());
    }

    @Test
    void searchUsersByRoleAndTermShouldReturnFilteredUsers() {
        // Arrange
        List<User> matchingUsers = List.of(adminUser);
        when(userRepository.findByRoleAndNameOrEmailContainingIgnoreCase(User.Role.ADMIN, "admin"))
                .thenReturn(matchingUsers);

        // Act
        List<User> result = userService.searchUsersByRoleAndTerm("ADMIN", "admin");

        // Assert
        assertEquals(1, result.size());
        assertEquals(User.Role.ADMIN, result.get(0).getRole());
        verify(userRepository, times(1))
                .findByRoleAndNameOrEmailContainingIgnoreCase(User.Role.ADMIN, "admin");
    }

    @Test
    void searchUsersByRoleAndEmptyTermShouldReturnAllUsersWithRole() {
        // Arrange
        List<User> adminUsers = List.of(adminUser);
        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(adminUsers);

        // Act
        List<User> result = userService.searchUsersByRoleAndTerm("ADMIN", "");

        // Assert
        assertEquals(1, result.size());
        assertEquals(User.Role.ADMIN, result.get(0).getRole());
        verify(userRepository, times(1)).findByRole(User.Role.ADMIN);
        verify(userRepository, never()).findByRoleAndNameOrEmailContainingIgnoreCase(any(), anyString());
    }

    @Test
    void getUserByEmailShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // Act
        User result = userService.getUserByEmail("admin@example.com");

        // Assert
        assertEquals(adminUser.getId(), result.getId());
        assertEquals(adminUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void getUserByEmailWithNonExistentEmailShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            userService.getUserByEmail("nonexistent@example.com");
        });
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void getUserByIdShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

        // Act
        User result = userService.getUserById(1);

        // Assert
        assertEquals(adminUser.getId(), result.getId());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void getUserByIdWithNonExistentIdShouldThrowException() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            userService.getUserById(999);
        });
        verify(userRepository, times(1)).findById(999);
    }

    @Test
    void updateUserRoleShouldChangeUserRole() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateUserRole(1, "ORGANIZER");

        // Assert
        assertEquals(User.Role.ORGANIZER, result.getRole());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(adminUser);
    }

    @Test
    void updateUserRoleWithInvalidRoleShouldThrowException() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRole(1, "INVALID_ROLE");
        });
        assertTrue(exception.getMessage().contains("Invalid role"));
    }

    @Test
    void updateUserShouldUpdateUserDetails() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateUser(1, "Updated Name", "updated@example.com", "123456789");

        // Assert
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("123456789", result.getPhoneNumber());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(adminUser);
    }

    @Test
    void updateUserPasswordShouldUpdateEncodedPassword() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encoded_new_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateUserPassword(1, "newPassword");

        // Assert
        assertEquals("encoded_new_password", result.getPassword());
        verify(userRepository, times(1)).findById(1);
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(adminUser);
    }

    @Test
    void deleteUserShouldDeleteUserById() {
        // Act
        userService.deleteUser(1);

        // Assert
        verify(userRepository, times(1)).deleteById(1);
    }
}