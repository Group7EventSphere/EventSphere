package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.dto.LoginUserDto;
import id.ac.ui.cs.advprog.eventspherre.dto.RegisterUserDto;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        // MockitoAnnotations.openMocks is done by @ExtendWith(MockitoExtension.class)
    }

    @Test
    void signup_encodesPasswordAndSavesUser() throws Exception {
        // arrange
        RegisterUserDto dto = new RegisterUserDto();
        setField(dto, "email",    "joe@example.com");
        setField(dto, "password", "plainPwd");
        setField(dto, "name",     "Joe");

        when(passwordEncoder.encode("plainPwd")).thenReturn("encodedPwd");
        User saved = new User();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        // act
        User result = authService.signup(dto);

        // assert
        assertSame(saved, result, "should return whatever repository.save returns");

        verify(passwordEncoder).encode("plainPwd");
        verify(userRepository).save(userCaptor.capture());

        User toSave = userCaptor.getValue();
        assertEquals("joe@example.com", toSave.getEmail());
        assertEquals("Joe",              toSave.getName());
        assertEquals("encodedPwd",       toSave.getPassword());
    }

    @Test
    void authenticate_callsManagerAndReturnsUser() throws Exception {
        // arrange
        LoginUserDto dto = new LoginUserDto();
        setField(dto, "email",    "anna@domain.com");
        setField(dto, "password", "herPass");

        User found = new User();
        found.setEmail("anna@domain.com");
        when(userRepository.findByEmail("anna@domain.com"))
            .thenReturn(Optional.of(found));

        // act
        User result = authService.authenticate(dto);

        // assert
        assertSame(found, result);
        verify(authenticationManager).authenticate(
            argThat(token ->
                token instanceof UsernamePasswordAuthenticationToken &&
                ((UsernamePasswordAuthenticationToken) token).getPrincipal().equals("anna@domain.com") &&
                ((UsernamePasswordAuthenticationToken) token).getCredentials().equals("herPass")
            )
        );
        verify(userRepository).findByEmail("anna@domain.com");
    }

    @Test
    void authenticate_userNotFound_throws() throws Exception {
        // arrange
        LoginUserDto dto = new LoginUserDto();
        setField(dto, "email",    "nobody@none.com");
        setField(dto, "password", "x");

        when(userRepository.findByEmail("nobody@none.com")).thenReturn(Optional.empty());

        // act & assert
        assertThrows(NoSuchElementException.class, () -> authService.authenticate(dto));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("nobody@none.com");
    }

    // --- reflection helper to set private fields on a Lombok @Getter-only DTO ---
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
