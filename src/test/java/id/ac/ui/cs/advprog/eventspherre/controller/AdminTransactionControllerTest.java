package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.SecurityConfiguration;
import id.ac.ui.cs.advprog.eventspherre.dto.PaymentTransactionDTO;
import id.ac.ui.cs.advprog.eventspherre.mapper.PaymentTransactionMapper;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.TransactionAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.containsString;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTransactionController.class)
@Import(SecurityConfiguration.class)
class AdminTransactionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TransactionAuditService auditService;

    @MockBean
    private PaymentTransactionMapper mapper;

    @MockBean
    private AuthenticationProvider authenticationProvider;  // satisfy SecurityConfiguration

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

    @Test
    void listActive_returns200_forAdmin() throws Exception {
        var dto = new PaymentTransactionDTO(
                UUID.randomUUID(),
                adminUser.getId(),
                10,
                "PURCHASE",
                "SUCCESS",
                Instant.now()
        );

        when(auditService.getActive()).thenReturn(List.of());
        when(mapper.toDtoList(List.of())).thenReturn(List.of(dto));

        mvc.perform(
                        get("/api/v1/admin/transactions")
                                .with(user(adminUser.getEmail()).roles(adminUser.getRole().name()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void listActive_redirectsToUnauthorized_forNonAdmin() throws Exception {
        mvc.perform(
                        get("/api/v1/admin/transactions")
                                .with(user(regularUser.getEmail()).roles(regularUser.getRole().name()))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/unauthorized")));
    }

    @Test
    void listActive_redirectsToLogin_forAnonymous() throws Exception {
        mvc.perform(get("/api/v1/admin/transactions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }
}
