package id.ac.ui.cs.advprog.eventspherre.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentTransactionDTO(
        UUID id,
        UUID userId,
        double amount,
        String type,
        String status,
        Instant createdAt
) {}