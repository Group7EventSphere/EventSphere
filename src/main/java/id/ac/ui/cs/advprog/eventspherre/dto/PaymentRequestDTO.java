package id.ac.ui.cs.advprog.eventspherre.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentRequestDTO(
    UUID    id,
    int     userId,
    double  amount,
    String  type,
    boolean processed,
    String  message,
    Instant createdAt
) {}
