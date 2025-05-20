package id.ac.ui.cs.advprog.eventspherre.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestDTOTest {

    @Test
    void constructorAndAccessors_workCorrectly() {
        UUID id         = UUID.randomUUID();
        int userId      = 42;
        double amount   = 123.45;
        String type     = "TOPUP";
        boolean processed = true;
        String message  = "Test Message";
        Instant created = Instant.now();

        PaymentRequestDTO dto = new PaymentRequestDTO(
            id, userId, amount, type, processed, message, created
        );

        assertEquals(id, dto.id());
        assertEquals(userId, dto.userId());
        assertEquals(amount, dto.amount());
        assertEquals(type, dto.type());
        assertTrue(dto.processed());
        assertEquals(message, dto.message());
        assertEquals(created, dto.createdAt());
    }
}
