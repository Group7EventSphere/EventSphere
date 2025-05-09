package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(
        properties = "spring.sql.init.mode=never"
)
class PaymentTransactionRepositoryTest {

    @Autowired
    private PaymentTransactionRepository repo;

    @Test
    @DisplayName("findAll() should return exactly the rows we save")
    void findAll_shouldReturnAllSavedTransactions() {
        var tx1 = PaymentTransaction.builder()
                .userId(UUID.randomUUID())
                .amount(10.0)
                .type("PURCHASE")
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();

        var tx2 = tx1.toBuilder()
                .status("SOFT_DELETED")
                .build();

        repo.saveAll(List.of(tx1, tx2));

        List<PaymentTransaction> all = repo.findAll();
        assertThat(all)
                .hasSize(2)
                .extracting(PaymentTransaction::getStatus)
                .containsExactlyInAnyOrder("SUCCESS","SOFT_DELETED");
    }

    @Test
    @DisplayName("findByStatusNot() should exclude soft-deleted rows only")
    void findByStatusNot_shouldExcludeSoftDeleted() {
        var tx1 = PaymentTransaction.builder()
                .userId(UUID.randomUUID())
                .amount(10.0)
                .type("PURCHASE")
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();

        var txDeleted = tx1.toBuilder()
                .status("SOFT_DELETED")
                .build();

        repo.saveAll(List.of(tx1, txDeleted));

        List<PaymentTransaction> active = repo.findByStatusNot("SOFT_DELETED");
        assertThat(active)
                .hasSize(1)
                .first()
                .extracting(PaymentTransaction::getStatus)
                .isEqualTo("SUCCESS");
    }
}
