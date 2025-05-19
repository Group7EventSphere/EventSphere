package id.ac.ui.cs.advprog.eventspherre.mapper;

import id.ac.ui.cs.advprog.eventspherre.dto.PaymentTransactionDTO;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest.PaymentType;   
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest.PaymentType.*;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentTransactionMapperTest {

    private PaymentTransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentTransactionMapper();
    }

    @Test
    void toDto_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        int userId = 505;
        double amount = 123.45;
        PaymentType type = TOPUP;
        String status = "SUCCESS";
        Instant now = Instant.now();

        PaymentTransaction tx = PaymentTransaction.builder()
                .id(id)
                .userId(userId)
                .amount(amount)
                .type(type)
                .status(status)
                .createdAt(now)
                .build();

        PaymentTransactionDTO dto = mapper.toDto(tx);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.amount()).isEqualTo(amount);
        assertThat(dto.type()).isEqualTo(type.name());
        assertThat(dto.status()).isEqualTo(status);
        assertThat(dto.createdAt()).isEqualTo(now);
    }

    @Test
    void toDtoList_shouldMapEachElement() {
        PaymentTransaction tx1 = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .userId(30)
                .amount(10.0)
                .type(PURCHASE)
                .status("FAILED")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        PaymentTransaction tx2 = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .userId(50)
                .amount(20.0)
                .type(TOPUP)
                .status("SUCCESS")
                .createdAt(Instant.parse("2025-02-02T12:34:56Z"))
                .build();

        List<PaymentTransactionDTO> dtoList = mapper.toDtoList(List.of(tx1, tx2));

        assertThat(dtoList).hasSize(2);

        assertThat(dtoList.get(0)).satisfies(dto -> {
            assertThat(dto.id()).isEqualTo(tx1.getId());
            assertThat(dto.userId()).isEqualTo(tx1.getUserId());
            assertThat(dto.amount()).isEqualTo(tx1.getAmount());
            assertThat(dto.type()).isEqualTo(tx1.getType().name());
            assertThat(dto.status()).isEqualTo(tx1.getStatus());
            assertThat(dto.createdAt()).isEqualTo(tx1.getCreatedAt());
        });

        assertThat(dtoList.get(1)).satisfies(dto -> {
            assertThat(dto.id()).isEqualTo(tx2.getId());
            assertThat(dto.userId()).isEqualTo(tx2.getUserId());
            assertThat(dto.amount()).isEqualTo(tx2.getAmount());
            assertThat(dto.type()).isEqualTo(tx2.getType().name());
            assertThat(dto.status()).isEqualTo(tx2.getStatus());
            assertThat(dto.createdAt()).isEqualTo(tx2.getCreatedAt());
        });
    }
}
