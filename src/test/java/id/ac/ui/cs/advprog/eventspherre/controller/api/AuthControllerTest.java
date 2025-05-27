package id.ac.ui.cs.advprog.eventspherre.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventspherre.config.WebSecurityTestConfig;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import id.ac.ui.cs.advprog.eventspherre.dto.*;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.AuthenticationService;
import id.ac.ui.cs.advprog.eventspherre.service.JwtService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(controllers = AuthController.class)
@Import(WebSecurityTestConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    private User testUser;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPhoneNumber("1234567890");
        testUser.setRole(User.Role.ATTENDEE);
        testUser.setBalance(100.0);

        testUserDetails = mock(UserDetails.class);
        when(testUserDetails.getUsername()).thenReturn("test@example.com");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnJwtResponse() throws Exception {
        // Arrange
        JwtAuthRequest loginRequest = new JwtAuthRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(testUserDetails);
        when(jwtService.generateToken(testUserDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUserDetails)).thenReturn("refresh-token");
        when(userService.findByEmail("test@example.com")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(AppConstants.JWT_EXPIRATION_TIME))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("ATTENDEE"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        JwtAuthRequest loginRequest = new JwtAuthRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void signup_WithValidData_ShouldReturnJwtResponse() throws Exception {
        // Arrange
        JwtRegisterRequest registerRequest = new JwtRegisterRequest();
        registerRequest.setName("New User");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password");
        registerRequest.setPhoneNumber("9876543210");
        registerRequest.setRole("USER");

        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);
        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(testUser);
        when(userService.updateUserRole(anyInt(), anyString())).thenReturn(testUser);
        when(userService.getUserById(anyInt())).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(testUserDetails);
        when(jwtService.generateToken(testUserDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUserDetails)).thenReturn("refresh-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void signup_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        JwtRegisterRequest registerRequest = new JwtRegisterRequest();
        registerRequest.setName("New User");
        registerRequest.setEmail("existing@example.com");
        registerRequest.setPassword("password");
        registerRequest.setPhoneNumber("9876543210");

        when(userService.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Email is already in use"));
    }

    @Test
    void signup_WithException_ShouldReturnBadRequest() throws Exception {
        // Arrange
        JwtRegisterRequest registerRequest = new JwtRegisterRequest();
        registerRequest.setName("New User");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password");
        registerRequest.setPhoneNumber("9876543210");

        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);
        when(authenticationService.signup(any(RegisterUserDto.class)))
                .thenThrow(new RuntimeException("Registration failed"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Registration failed"));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAccessToken() throws Exception {
        // Arrange
        JwtRefreshRequest refreshRequest = new JwtRefreshRequest();
        refreshRequest.setRefreshToken("valid-refresh-token");

        when(jwtService.extractUsername("valid-refresh-token")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("valid-refresh-token", testUserDetails)).thenReturn(true);
        when(jwtService.generateToken(testUserDetails)).thenReturn("new-access-token");
        when(userService.findByEmail("test@example.com")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("valid-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        JwtRefreshRequest refreshRequest = new JwtRefreshRequest();
        refreshRequest.setRefreshToken("invalid-refresh-token");

        when(jwtService.extractUsername("invalid-refresh-token")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("invalid-refresh-token", testUserDetails)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void refreshToken_WithException_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        JwtRefreshRequest refreshRequest = new JwtRefreshRequest();
        refreshRequest.setRefreshToken("refresh-token");

        when(jwtService.extractUsername("refresh-token"))
                .thenThrow(new RuntimeException("Token parsing failed"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCurrentUser_WithValidAuthentication_ShouldReturnUserInfo() throws Exception {
        // Arrange
        when(userService.findByEmail("test@example.com")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"))
                .andExpect(jsonPath("$.role").value("ATTENDEE"))
                .andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    void getCurrentUser_WithException_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
