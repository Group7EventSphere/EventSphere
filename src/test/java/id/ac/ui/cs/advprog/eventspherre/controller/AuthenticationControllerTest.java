package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.dto.LoginUserDto;
import id.ac.ui.cs.advprog.eventspherre.dto.RegisterUserDto;
import id.ac.ui.cs.advprog.eventspherre.dto.LoginResponse;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.AuthenticationService;
import id.ac.ui.cs.advprog.eventspherre.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private JwtService jwtService; 

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController controller;

    @Test
    void register_shouldReturn200WithSavedUser() throws Exception {
        // Arrange
        var dto = new RegisterUserDto();
        setField(dto, "email",    "joe@example.com");
        setField(dto, "password", "secret");
        setField(dto, "name",     "Joe");

        var saved = new User();
        saved.setId(99);
        saved.setEmail("joe@example.com");
        saved.setName("Joe");

        when(authenticationService.signup(dto)).thenReturn(saved);

        // Act
        ResponseEntity<User> resp = controller.register(dto);

        // Assert
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isSameAs(saved);

        verify(authenticationService).signup(dto);
        verifyNoMoreInteractions(jwtService);
    }

    @Test
    void authenticate_shouldReturn200WithTokenAndExpiry() throws Exception {
        // Arrange
        var dto = new LoginUserDto();
        setField(dto, "email",    "anna@example.com");
        setField(dto, "password", "pw123");

        var user = new User();
        user.setId(42);
        user.setEmail("anna@example.com");
        user.setName("Anna");

        when(authenticationService.authenticate(dto)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token-xyz");
        when(jwtService.getExpirationTime()).thenReturn(3_600L);

        // Act
        ResponseEntity<LoginResponse> resp = controller.authenticate(dto);

        // Assert
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getToken()).isEqualTo("jwt-token-xyz");
        assertThat(body.getExpiresIn()).isEqualTo(3_600L);

        InOrder inOrder = inOrder(authenticationService, jwtService);
        inOrder.verify(authenticationService).authenticate(dto);
        inOrder.verify(jwtService).generateToken(user);
        inOrder.verify(jwtService).getExpirationTime();
    }

    // reflection helper to set private Lombok fields
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
