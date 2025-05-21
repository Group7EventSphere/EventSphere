package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.SecurityConfiguration;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAuditViewController.class)
@Import(SecurityConfiguration.class)
class AdminAuditViewControllerTest {

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
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
    }

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void auditPage_returnsAdminAuditView_asAdmin() throws Exception {
        mockMvc.perform(
                        get("/admin/audit_admin")
                                // use the email & role from setUp()
                                .with(user(adminUser.getEmail()).roles(adminUser.getRole().name()))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("admin_audit"));
    }

    @Test
    void auditPage_forbidden_forNonAdmin() throws Exception {
        mockMvc.perform(
                        get("/admin/audit_admin")
                                .with(user(regularUser.getEmail()).roles(regularUser.getRole().name()))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/unauthorized")));
    }

    @Test
    void auditPage_redirectsToLogin_forAnonymous() throws Exception {
        mockMvc.perform(get("/admin/audit_admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }
}
