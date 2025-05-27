package id.ac.ui.cs.advprog.eventspherre.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTransactionDTOTest {

    @Test
    void recordAccessorsReturnConstructorValues() {
        UUID id       = UUID.randomUUID();
        int userId   = 10;
        double amount = 99.99;
        String type   = "PURCHASE";
        String status = "FAILED";
        Instant now   = Instant.now();

        PaymentTransactionDTO dto = new PaymentTransactionDTO(
                id, userId, amount, type, status, now
        );

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.amount()).isEqualTo(amount);
        assertThat(dto.type()).isEqualTo(type);
        assertThat(dto.status()).isEqualTo(status);
        assertThat(dto.createdAt()).isEqualTo(now);
    }

    @Test
    void equalsAndHashCode_workAsExpected() {
        UUID id     = UUID.randomUUID();
        Instant ts  = Instant.parse("2025-05-09T12:00:00Z");

        PaymentTransactionDTO a = new PaymentTransactionDTO(
                id, 11, 10.0, "TOPUP", "SUCCESS", ts
        );
        PaymentTransactionDTO b = new PaymentTransactionDTO(
                id, a.userId(),         10.0, "TOPUP", "SUCCESS", ts
        );
        PaymentTransactionDTO c = new PaymentTransactionDTO(
                UUID.randomUUID(), a.userId(), 10.0, "TOPUP", "SUCCESS", ts
        );

        assertThat(a)
            .isEqualTo(b)
            .hasSameHashCodeAs(b)
            .isNotEqualTo(c);
    }

    @Test
    void toString_includesAllFields() {
        UUID id     = UUID.fromString("00000000-0000-0000-0000-000000000001");
        int userId = 50;
        double amt  = 42.42;
        String type = "TOPUP";
        String stat = "SUCCESS";
        Instant ts  = Instant.parse("2025-05-09T12:34:56Z");

        PaymentTransactionDTO dto = new PaymentTransactionDTO(
                id, 50, amt, type, stat, ts
        );

        String s = dto.toString();

        assertThat(s)
                .contains("PaymentTransactionDTO")
                .contains(id.toString())
                .contains(String.valueOf(userId))
                .contains(Double.toString(amt))
                .contains(type)
                .contains(stat)
                .contains(ts.toString());
    }
}
