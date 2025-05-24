package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.SecurityConfiguration;
import id.ac.ui.cs.advprog.eventspherre.dto.PaymentTransactionDTO;
import id.ac.ui.cs.advprog.eventspherre.mapper.PaymentTransactionMapper;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.TransactionAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private AuthenticationProvider authenticationProvider;

    private User admin;
    private User attendee;
    private UUID sampleId;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1);
        admin.setEmail("admin@example.com");
        admin.setRole(User.Role.ADMIN);

        attendee = new User();
        attendee.setId(2);
        attendee.setEmail("user@example.com");
        attendee.setRole(User.Role.ATTENDEE);

        sampleId = UUID.randomUUID();
    }

    @Test
    void listActive_okForAdmin() throws Exception {
        var dto = new PaymentTransactionDTO(
                sampleId, admin.getId(), 10, "PURCHASE", "SUCCESS", Instant.now());

        when(auditService.getActive()).thenReturn(List.of());
        when(mapper.toDtoList(List.of())).thenReturn(List.of(dto));

        mvc.perform(get("/api/v1/admin/transactions")
                       .with(user(admin.getEmail()).roles(admin.getRole().name())))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void listAll_okWhenAllTrue() throws Exception {
        when(auditService.getAll()).thenReturn(List.of());
        when(mapper.toDtoList(List.of())).thenReturn(List.of());

        mvc.perform(get("/api/v1/admin/transactions?all=true")
                       .with(user(admin.getEmail()).roles("ADMIN")))
           .andExpect(status().isOk());

        verify(auditService).getAll();
        verify(auditService, never()).getActive();
    }

    @Test
    void listActive_redirectsForNonAdmin() throws Exception {
        mvc.perform(get("/api/v1/admin/transactions")
                       .with(user(attendee.getEmail()).roles("ATTENDEE")))
           .andExpect(status().is3xxRedirection())
           .andExpect(header().string("Location", containsString("/unauthorized")));
    }

    @Test
    void listActive_redirectsForAnonymous() throws Exception {
        mvc.perform(get("/api/v1/admin/transactions"))
           .andExpect(status().is3xxRedirection())
           .andExpect(header().string("Location", containsString("/login")));
    }

    @Test
    void markFailed_invokesService() throws Exception {
        mvc.perform(put("/api/v1/admin/transactions/{id}/failed", sampleId)
                       .with(user(admin.getEmail()).roles("ADMIN"))
                       .with(csrf()))
           .andExpect(status().isNoContent());

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(auditService).flagFailed(captor.capture());
        assertThat(captor.getValue()).isEqualTo(sampleId);
    }

    @Test
    void softDelete_invokesService() throws Exception {
        mvc.perform(delete("/api/v1/admin/transactions/{id}", sampleId)
                       .with(user(admin.getEmail()).roles("ADMIN"))
                       .with(csrf()))
           .andExpect(status().isNoContent());

        verify(auditService).softDelete(sampleId);
    }

    @Test
    void hardDelete_invokesService() throws Exception {
        mvc.perform(delete("/api/v1/admin/transactions/{id}/hard", sampleId)
                       .with(user(admin.getEmail()).roles("ADMIN"))
                       .with(csrf()))
           .andExpect(status().isNoContent());

        verify(auditService).hardDelete(sampleId);
    }
}
