package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.dto.PaymentTransactionDTO;
import id.ac.ui.cs.advprog.eventspherre.mapper.PaymentTransactionMapper;
import id.ac.ui.cs.advprog.eventspherre.service.TransactionAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTransactionController.class)
class AdminTransactionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TransactionAuditService auditService;

    @MockBean
    private PaymentTransactionMapper mapper;

    @Test
    void listActive_returns200() throws Exception {
        var dto = new PaymentTransactionDTO(
                UUID.randomUUID(), UUID.randomUUID(), 10,
                "PURCHASE", "SUCCESS", Instant.now());
        when(auditService.getActive()).thenReturn(List.of());
        when(mapper.toDtoList(List.of())).thenReturn(List.of(dto));

        mvc.perform(get("/api/v1/admin/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }
}
