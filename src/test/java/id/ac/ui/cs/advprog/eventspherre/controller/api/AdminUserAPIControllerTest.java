package id.ac.ui.cs.advprog.eventspherre.controller.api;

import id.ac.ui.cs.advprog.eventspherre.config.WebSecurityTestConfig;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AdminUserAPIController.class)
@Import(WebSecurityTestConfig.class)
@ActiveProfiles("test")
class AdminUserAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPhoneNumber("1234567890");
        testUser.setRole(User.Role.ATTENDEE);
        testUser.setBalance(100.0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WithValidId_ShouldReturnUser() throws Exception {
        // Arrange
        when(userService.findById(1)).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"))
                .andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserById_WithNonAdminRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isForbidden());
    }    @Test
    void getUserById_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert - Spring Security redirects unauthenticated requests to login (302)
        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isFound()); // 302 redirect to login
    }
}
