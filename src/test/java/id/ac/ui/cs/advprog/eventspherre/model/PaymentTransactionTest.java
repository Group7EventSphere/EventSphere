package id.ac.ui.cs.advprog.eventspherre.model;

import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest.PaymentType;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest.PaymentType.*;

class PaymentTransactionTest {

    @Test
    void builderAndToBuilder_shouldProduceCorrectInstances() {
        // arrange
        UUID id = UUID.randomUUID();
        int userId = 50;
        double amount = 150.5;
        PaymentType type = PURCHASE;
        String status = "SUCCESS";
        Instant now = Instant.now();

        // act: build initial instance
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(id)
                .userId(userId)
                .amount(amount)
                .type(type)
                .status(status)
                .createdAt(now)
                .build();

        // assert initial values
        assertThat(tx.getId()).isEqualTo(id);
        assertThat(tx.getUserId()).isEqualTo(userId);
        assertThat(tx.getAmount()).isEqualTo(amount);
        assertThat(tx.getType()).isEqualTo(type);
        assertThat(tx.getStatus()).isEqualTo(status);
        assertThat(tx.getCreatedAt()).isEqualTo(now);

        // act: modify via toBuilder
        PaymentTransaction modified = tx.toBuilder()
                .status("FAILED")
                .build();

        // assert modified value and preserved fields
        assertThat(modified.getStatus()).isEqualTo("FAILED");
        assertThat(modified.getId()).isEqualTo(id);
        assertThat(modified.getUserId()).isEqualTo(userId);
        assertThat(modified.getAmount()).isEqualTo(amount);
        assertThat(modified.getType()).isEqualTo(type);
        assertThat(modified.getCreatedAt()).isEqualTo(now);
    }
}