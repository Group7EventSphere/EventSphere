package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.dto.PaymentTransactionDTO;
import id.ac.ui.cs.advprog.eventspherre.mapper.PaymentTransactionMapper;
import id.ac.ui.cs.advprog.eventspherre.service.TransactionAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final TransactionAuditService auditService;
    private final PaymentTransactionMapper mapper;

    @GetMapping
    public List<PaymentTransactionDTO> list(
            @RequestParam(defaultValue = "false") boolean all) {

        return all
                ? mapper.toDtoList(auditService.getAll())
                : mapper.toDtoList(auditService.getActive());
    }

    @PutMapping("/{id}/failed")
    public ResponseEntity<Void> markFailed(@PathVariable UUID id) {
        auditService.flagFailed(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        auditService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
