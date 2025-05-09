package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @AfterEach
    void tearDown() {
        // avoid leaking auth between tests
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedUser_shouldReturnPrincipal() {
        // Arrange: create a dummy User and put into SecurityContext
        User principal = new User();
        principal.setId(123);
        principal.setEmail("test@example.com");
        principal.setName("Test User");

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        ResponseEntity<User> response = controller.authenticatedUser();

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(principal);
    }

    @Test
    void allUsers_shouldReturnListFromService() {
        // Arrange: mock two users
        User u1 = new User();
        u1.setId(1);
        u1.setEmail("a@a.com");
        u1.setName("A");

        User u2 = new User();
        u2.setId(2);
        u2.setEmail("b@b.com");
        u2.setName("B");

        List<User> mockList = Arrays.asList(u1, u2);
        when(userService.allUsers()).thenReturn(mockList);

        // Act
        ResponseEntity<List<User>> response = controller.allUsers();

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(mockList);

        verify(userService).allUsers();
        verifyNoMoreInteractions(userService);
    }
}
