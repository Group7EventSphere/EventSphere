package id.ac.ui.cs.advprog.eventspherre.mapper;

import id.ac.ui.cs.advprog.eventspherre.dto.PaymentTransactionDTO;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTransactionMapperTest {

    private PaymentTransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentTransactionMapper();
    }

    @Test
    void toDto_shouldMapAllFields() {
        UUID id       = UUID.randomUUID();
        UUID userId   = UUID.randomUUID();
        double amount = 123.45;
        String type   = "TOPUP";
        String status = "SUCCESS";
        Instant now   = Instant.now();

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
        assertThat(dto.type()).isEqualTo(type);
        assertThat(dto.status()).isEqualTo(status);
        assertThat(dto.createdAt()).isEqualTo(now);
    }

    @Test
    void toDtoList_shouldMapEachElement() {
        PaymentTransaction tx1 = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(10.0)
                .type("PURCHASE")
                .status("FAILED")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        PaymentTransaction tx2 = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(20.0)
                .type("TOPUP")
                .status("SUCCESS")
                .createdAt(Instant.parse("2025-02-02T12:34:56Z"))
                .build();

        List<PaymentTransactionDTO> dtoList = mapper.toDtoList(List.of(tx1, tx2));

        assertThat(dtoList).hasSize(2);

        // verify first element
        PaymentTransactionDTO dto1 = dtoList.get(0);
        assertThat(dto1.id()).isEqualTo(tx1.getId());
        assertThat(dto1.userId()).isEqualTo(tx1.getUserId());
        assertThat(dto1.amount()).isEqualTo(tx1.getAmount());
        assertThat(dto1.type()).isEqualTo(tx1.getType());
        assertThat(dto1.status()).isEqualTo(tx1.getStatus());
        assertThat(dto1.createdAt()).isEqualTo(tx1.getCreatedAt());

        // verify second element
        PaymentTransactionDTO dto2 = dtoList.get(1);
        assertThat(dto2.id()).isEqualTo(tx2.getId());
        assertThat(dto2.userId()).isEqualTo(tx2.getUserId());
        assertThat(dto2.amount()).isEqualTo(tx2.getAmount());
        assertThat(dto2.type()).isEqualTo(tx2.getType());
        assertThat(dto2.status()).isEqualTo(tx2.getStatus());
        assertThat(dto2.createdAt()).isEqualTo(tx2.getCreatedAt());
    }
}
