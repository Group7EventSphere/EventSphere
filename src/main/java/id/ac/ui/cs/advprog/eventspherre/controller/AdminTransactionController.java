package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.dto.PaymentTransactionDTO;
import id.ac.ui.cs.advprog.eventspherre.mapper.PaymentTransactionMapper;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest.PaymentType;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import id.ac.ui.cs.advprog.eventspherre.service.TransactionAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final TransactionAuditService auditService;
    private final PaymentTransactionMapper mapper;
    private final UserRepository userRepository;

    @GetMapping
    public List<PaymentTransactionDTO> list(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "false") boolean all) {

        List<PaymentTransaction> transactions = all
                ? auditService.getAll()
                : auditService.getActive();

        if (userName != null && !userName.isEmpty()) {
            List<User> matchingUsers = userRepository.findByNameContainingIgnoreCase(userName);
            List<Integer> userIds = matchingUsers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            
            transactions = transactions.stream()
                    .filter(t -> userIds.contains(t.getUserId()))
                    .collect(Collectors.toList());
        }

        if (userEmail != null && !userEmail.isEmpty()) {
            List<User> matchingUsers = userRepository.findByEmailContainingIgnoreCase(userEmail);
            List<Integer> userIds = matchingUsers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            
            transactions = transactions.stream()
                    .filter(t -> userIds.contains(t.getUserId()))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            transactions = transactions.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        if (type != null && !type.isEmpty()) {
            try {
                PaymentType paymentType = PaymentType.valueOf(type.toUpperCase());
                transactions = transactions.stream()
                        .filter(t -> t.getPaymentType() == paymentType)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
            }
        }

        return mapper.toDtoList(transactions);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        
        if ("FAILED".equalsIgnoreCase(status)) {
            auditService.flagFailed(id);
        } else if ("SUCCESS".equalsIgnoreCase(status)) {
            auditService.markSuccess(id);
        }
        
        return ResponseEntity.noContent().build();
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

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable UUID id) {
        auditService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/success")
    public ResponseEntity<Void> markSuccess(@PathVariable UUID id) {
    auditService.markSuccess(id);
    return ResponseEntity.noContent().build();
}
}
