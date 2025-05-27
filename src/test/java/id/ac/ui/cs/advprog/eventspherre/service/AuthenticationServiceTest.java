package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.dto.RegisterUserDto;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private RegisterUserDto regularUserDto;
    private RegisterUserDto adminUserDto;
    private RegisterUserDto organizerUserDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        regularUserDto = new RegisterUserDto();
        regularUserDto.setEmail("john@example.com");
        regularUserDto.setPassword("password123");
        regularUserDto.setName("John Doe");
        regularUserDto.setPhoneNumber("1234567890");

        adminUserDto = new RegisterUserDto();
        adminUserDto.setEmail("admin@example.com");
        adminUserDto.setPassword("adminpass");
        adminUserDto.setName("Admin User");
        adminUserDto.setPhoneNumber("9876543210");

        organizerUserDto = new RegisterUserDto();
        organizerUserDto.setEmail("organizer@events.com");
        organizerUserDto.setPassword("orgpass");
        organizerUserDto.setName("Event Organizer");
        organizerUserDto.setPhoneNumber("5555555555");
    }

    @Test
    void signupRegularUserShouldCreateAttendeeUser() {
        // Arrange
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.signup(regularUserDto);

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("encoded_password", savedUser.getPassword());
        assertEquals("1234567890", savedUser.getPhoneNumber());
        assertEquals(User.Role.ATTENDEE, savedUser.getRole());
    }

    @Test
    void signupWithAdminEmailShouldCreateAdminUser() {
        // Arrange
        when(passwordEncoder.encode("adminpass")).thenReturn("encoded_admin_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.signup(adminUserDto);

        // Assert
        verify(passwordEncoder).encode("adminpass");
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("admin@example.com", savedUser.getEmail());
        assertEquals(User.Role.ADMIN, savedUser.getRole());
    }

    @Test
    void signupWithOrganizerEmailShouldCreateOrganizerUser() {
        // Arrange
        when(passwordEncoder.encode("orgpass")).thenReturn("encoded_org_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.signup(organizerUserDto);

        // Assert
        verify(passwordEncoder).encode("orgpass");
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("organizer@events.com", savedUser.getEmail());
        assertEquals(User.Role.ORGANIZER, savedUser.getRole());
    }

    @Test
    void signupWithNullEmailShouldCreateAttendeeUser() {
        // Arrange
        RegisterUserDto nullEmailDto = new RegisterUserDto();
        nullEmailDto.setEmail(null);
        nullEmailDto.setPassword("password");
        nullEmailDto.setName("No Email User");

        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = authService.signup(nullEmailDto);

        // Assert
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNull(savedUser.getEmail());
        assertEquals(User.Role.ATTENDEE, savedUser.getRole());
    }

    @Test
    void signupShouldReturnSavedUser() {
        // Arrange
        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setEmail("john@example.com");
        savedUser.setName("John Doe");
        savedUser.setPassword("encoded_password");
        savedUser.setRole(User.Role.ATTENDEE);

        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = authService.signup(regularUserDto);

        // Assert
        assertSame(savedUser, result);
        assertEquals(1, result.getId());
    }
}
