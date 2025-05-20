package id.ac.ui.cs.advprog.eventspherre.mapper;

import id.ac.ui.cs.advprog.eventspherre.dto.PaymentRequestDTO;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestMapperTest {

    private PaymentRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentRequestMapper();

    }

    @Test
    void entityToDtoAndBack() {
        UUID id       = UUID.randomUUID();
        int userId    = 24;
        double amount = 200.0;
        PaymentRequest.PaymentType type = PaymentRequest.PaymentType.PURCHASE;
        boolean processed = false;
        String message     = "Init";
        Instant created    = Instant.now();

        // build entity
        PaymentRequest entity = PaymentRequest.builder()
                .id(id)
                .userId(userId)
                .amount(amount)
                .type(type)
                .processed(processed)
                .message(message)
                .createdAt(created)
                .build();

        // map to DTO
        PaymentRequestDTO dto = mapper.toDto(entity);
        assertEquals(id,          dto.id());
        assertEquals(userId,      dto.userId());
        assertEquals(amount,      dto.amount());
        assertEquals(type.name(), dto.type());
        assertEquals(processed,   dto.processed());
        assertEquals(message,     dto.message());
        assertEquals(created,     dto.createdAt());

        // map back to entity
        PaymentRequest entity2 = mapper.toEntity(dto);
        assertEquals(entity.getId(),        entity2.getId());
        assertEquals(entity.getUserId(),    entity2.getUserId());
        assertEquals(entity.getAmount(),    entity2.getAmount());
        assertEquals(entity.getType(),      entity2.getType());
        assertEquals(entity.isProcessed(),  entity2.isProcessed());
        assertEquals(entity.getMessage(),   entity2.getMessage());
        assertEquals(entity.getCreatedAt(), entity2.getCreatedAt());
    }
}
