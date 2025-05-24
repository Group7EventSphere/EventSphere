package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.WebSecurityTestConfig;
import id.ac.ui.cs.advprog.eventspherre.dto.PaymentTransactionDTO;
import id.ac.ui.cs.advprog.eventspherre.mapper.PaymentTransactionMapper;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import id.ac.ui.cs.advprog.eventspherre.service.TransactionAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
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
@Import(WebSecurityTestConfig.class)
@ActiveProfiles("test")
class AdminTransactionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TransactionAuditService auditService;

    @MockBean
    private PaymentTransactionMapper mapper;
    
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    private User admin;
    private User attendee;
    private UUID sampleId;
    private UUID transactionId;
    private PaymentTransaction transaction;
    private PaymentTransactionDTO transactionDTO;

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
        attendee.setName("Regular User");

        sampleId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        
        transaction = new PaymentTransaction();
        transaction.setId(transactionId);
        transaction.setUserId(attendee.getId());
        transaction.setAmount(100.0);
        transaction.setType(PaymentRequest.PaymentType.TOPUP);
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(Instant.now());
        
        transactionDTO = new PaymentTransactionDTO(
                transactionId, attendee.getId(), 100.0, "TOPUP", "SUCCESS", Instant.now());
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
    void listFiltered_returnsFilteredResults() throws Exception {
        // Test filtering by user name
        when(userRepository.findByNameContainingIgnoreCase("Regular")).thenReturn(List.of(attendee));
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(any())).thenReturn(List.of(transactionDTO));

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("userName", "Regular")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(attendee.getId()));
        
        // Test filtering by status
        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("status", "SUCCESS")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
        
        // Test filtering by type
        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("type", "TOPUP")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TOPUP"));
    }
    
    @Test
    void updateStatus_shouldCallCorrectServiceMethod() throws Exception {
        // Test marking as failed
        mvc.perform(
                put("/api/v1/admin/transactions/{id}/status", transactionId)
                        .param("status", "FAILED")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isNoContent());
        
        verify(auditService).flagFailed(transactionId);
        
        // Test marking as successful
        mvc.perform(
                put("/api/v1/admin/transactions/{id}/status", transactionId)
                        .param("status", "SUCCESS")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isNoContent());
        
        verify(auditService).markSuccess(transactionId);
    }
    
    @Test
    void softDelete_shouldCallService() throws Exception {
        mvc.perform(
                delete("/api/v1/admin/transactions/{id}", transactionId)
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isNoContent());
        
        verify(auditService).softDelete(transactionId);
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
    void listActive_forbiddenForNonAdmin() throws Exception {
        mvc.perform(get("/api/v1/admin/transactions")
                       .with(user(attendee.getEmail()).roles("ATTENDEE")))
           .andExpect(status().isForbidden());
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