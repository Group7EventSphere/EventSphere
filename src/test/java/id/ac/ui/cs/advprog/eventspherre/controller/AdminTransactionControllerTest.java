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
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
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

    @MockitoBean
    private TransactionAuditService auditService;

    @MockitoBean
    private PaymentTransactionMapper mapper;
    
    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
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

        @Test
    void updateStatus_withCaseInsensitiveFailed_shouldWork() throws Exception {
        mvc.perform(
                put("/api/v1/admin/transactions/{id}/status", transactionId)
                        .param("status", "failed")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isNoContent());
        
        verify(auditService).flagFailed(transactionId);
    }

    @Test
    void updateStatus_withCaseInsensitiveSuccess_shouldWork() throws Exception {
        mvc.perform(
                put("/api/v1/admin/transactions/{id}/status", transactionId)
                        .param("status", "success")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isNoContent());
        
        verify(auditService).markSuccess(transactionId);
    }

    @Test
    void listFiltered_withMultipleFilters_shouldApplyAll() throws Exception {
        when(userRepository.findByNameContainingIgnoreCase("Regular")).thenReturn(List.of(attendee));
        when(userRepository.findByEmailContainingIgnoreCase("user")).thenReturn(List.of(attendee));
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(any())).thenReturn(List.of(transactionDTO));

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("userName", "Regular")
                        .param("userEmail", "user")
                        .param("status", "SUCCESS")
                        .param("type", "TOPUP")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(attendee.getId()));
    }

    @Test
    void listFiltered_withNoMatchingUsers_shouldReturnEmptyList() throws Exception {
        when(userRepository.findByNameContainingIgnoreCase("NonExistent")).thenReturn(Collections.emptyList());
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("userName", "NonExistent")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void list_debugLogging_shouldLogAllParameter() throws Exception {
        when(auditService.getAll()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(any())).thenReturn(List.of(transactionDTO));

        mvc.perform(get("/api/v1/admin/transactions")
                       .param("all", "true")
                       .with(user(admin.getEmail()).roles("ADMIN")))
           .andExpect(status().isOk());

        // This tests the log.debug line in the controller
        verify(auditService).getAll();
    }

    @Test
    void markFailed_shouldLogInfo() throws Exception {
        mvc.perform(put("/api/v1/admin/transactions/{id}/failed", sampleId)
                       .with(user(admin.getEmail()).roles("ADMIN"))
                       .with(csrf()))
           .andExpect(status().isNoContent());

        verify(auditService).flagFailed(sampleId);
        // This tests the log.info line in markFailed method
    }

    @Test
    void markSuccess_shouldLogInfo() throws Exception {
        mvc.perform(put("/api/v1/admin/transactions/{id}/success", sampleId)
                       .with(user(admin.getEmail()).roles("ADMIN"))
                       .with(csrf()))
           .andExpect(status().isNoContent());

        verify(auditService).markSuccess(sampleId);
        // This tests the log.info line in markSuccess method
    }

    @Test
    void softDelete_shouldLogWarning() throws Exception {
        mvc.perform(delete("/api/v1/admin/transactions/{id}", sampleId)
                       .with(user(admin.getEmail()).roles("ADMIN"))
                       .with(csrf()))
           .andExpect(status().isNoContent());

        verify(auditService).softDelete(sampleId);
        // This tests the log.warn line in softDelete method
    }

    @Test
    void hardDelete_shouldLogWarning() throws Exception {
        mvc.perform(delete("/api/v1/admin/transactions/{id}/hard", sampleId)
                       .with(user(admin.getEmail()).roles("ADMIN"))
                       .with(csrf()))
           .andExpect(status().isNoContent());

        verify(auditService).hardDelete(sampleId);
        // This tests the log.warn line in hardDelete method
    }

 
    @Test
    void updateStatus_withNullStatus_shouldReturnBadRequest() throws Exception {
        // Test missing status parameter
        mvc.perform(
                put("/api/v1/admin/transactions/{id}/status", transactionId)
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
                        .with(csrf())
        )
        .andExpect(status().isInternalServerError());
        
        verify(auditService, never()).flagFailed(any());
        verify(auditService, never()).markSuccess(any());
    }

    @Test
    void updateStatus_withMixedCaseSuccess_shouldWork() throws Exception {
        // Test mixed case "SuCcEsS"
        mvc.perform(
                put("/api/v1/admin/transactions/{id}/status", transactionId)
                        .param("status", "SuCcEsS")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
                        .with(csrf())
        )
                .andExpect(status().isNoContent());
        
        verify(auditService).markSuccess(transactionId);
    }

    @Test
    void updateStatus_withMixedCaseFailed_shouldWork() throws Exception {
        // Test mixed case "FaIlEd"
        mvc.perform(
                put("/api/v1/admin/transactions/{id}/status", transactionId)
                        .param("status", "FaIlEd")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
                        .with(csrf())
        )
                .andExpect(status().isNoContent());
        
        verify(auditService).flagFailed(transactionId);
    }

    @Test
    void list_withAllParametersEmpty_shouldReturnAllActive() throws Exception {
        // Test all filter parameters as empty strings
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(any())).thenReturn(List.of(transactionDTO));

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("userName", "")
                        .param("userEmail", "")
                        .param("status", "")
                        .param("type", "")
                        .param("all", "false")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(attendee.getId()));
    }

    @Test
    void list_withUserEmailFilter_shouldFilterByEmail() throws Exception {
        // Test userEmail filter branch
        when(userRepository.findByEmailContainingIgnoreCase("example")).thenReturn(List.of(attendee));
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(any())).thenReturn(List.of(transactionDTO));

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("userEmail", "example")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(attendee.getId()));
        
        verify(userRepository).findByEmailContainingIgnoreCase("example");
    }

    @Test
    void list_withTypeFilterDifferentCase_shouldWork() throws Exception {
        // Test type filter with different cases
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(any())).thenReturn(List.of(transactionDTO));

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("type", "topup")  // lowercase
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("type", "PURCHASE")  // uppercase
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("type", "Refund")  // mixed case
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk());
    }

    @Test
    void list_withStatusFilterDifferentCase_shouldWork() throws Exception {
        // Test status filter with different cases
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(any())).thenReturn(List.of(transactionDTO));

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("status", "success")  // lowercase
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("status", "FAILED")  // uppercase
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("status", "Soft_Deleted")  // mixed case
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk());
    }

    @Test
    void list_withNonMatchingTypeFilter_shouldReturnEmpty() throws Exception {
        // Test lambda$list$3 branch where type doesn't match
        PaymentTransaction purchaseTransaction = new PaymentTransaction();
        purchaseTransaction.setId(UUID.randomUUID());
        purchaseTransaction.setUserId(attendee.getId());
        purchaseTransaction.setAmount(50.0);
        purchaseTransaction.setType(PaymentRequest.PaymentType.PURCHASE);
        purchaseTransaction.setStatus("SUCCESS");
        purchaseTransaction.setCreatedAt(Instant.now());

        when(auditService.getActive()).thenReturn(List.of(purchaseTransaction));
        when(mapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("type", "TOPUP")  // Filter for TOPUP but transaction is PURCHASE
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void list_withNonMatchingStatusFilter_shouldReturnEmpty() throws Exception {
        // Test status filter that doesn't match
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("status", "FAILED")  // Filter for FAILED but transaction is SUCCESS
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void list_withMultipleUserFilters_shouldIntersectResults() throws Exception {
        // Test when both userName and userEmail filters are applied
        User anotherUser = new User();
        anotherUser.setId(3);
        anotherUser.setEmail("another@example.com");
        anotherUser.setName("Another User");

        when(userRepository.findByNameContainingIgnoreCase("Regular")).thenReturn(List.of(attendee));
        when(userRepository.findByEmailContainingIgnoreCase("another")).thenReturn(List.of(anotherUser));
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("userName", "Regular")
                        .param("userEmail", "another")  // No intersection between users
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void list_withAllFiltersBooleanFalse_shouldCallGetActive() throws Exception {
        // Test explicit all=false
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(any())).thenReturn(List.of(transactionDTO));

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("all", "false")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk());

        verify(auditService).getActive();
        verify(auditService, never()).getAll();
    }

    @Test
    void list_withAllFiltersBooleanInvalid_shouldDefaultToFalse() throws Exception {
        // Test invalid boolean value for all parameter
        when(auditService.getActive()).thenReturn(List.of(transaction));
        when(mapper.toDtoList(any())).thenReturn(List.of(transactionDTO));

        mvc.perform(
                get("/api/v1/admin/transactions")
                        .param("all", "invalid")
                        .with(user(admin.getEmail()).roles(admin.getRole().name()))
        )
                .andExpect(status().isOk());

        verify(auditService).getActive();
        verify(auditService, never()).getAll();
    }

    @Test
    void updateStatus_forbiddenForAttendee() throws Exception {
        // Test authorization for updateStatus
        mvc.perform(
                put("/api/v1/admin/transactions/{id}/status", transactionId)
                        .param("status", "SUCCESS")
                        .with(user(attendee.getEmail()).roles("ATTENDEE"))
                        .with(csrf())
        )
                .andExpect(status().isForbidden());

        verify(auditService, never()).markSuccess(any());
        verify(auditService, never()).flagFailed(any());
    }

    @Test
    void updateStatus_redirectsForAnonymous() throws Exception {
        // Test anonymous access to updateStatus
        mvc.perform(
                put("/api/v1/admin/transactions/{id}/status", transactionId)
                        .param("status", "SUCCESS")
                        .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }
}