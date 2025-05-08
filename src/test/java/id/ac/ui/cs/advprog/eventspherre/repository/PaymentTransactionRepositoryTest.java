package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentTransactionRepositoryTest {

    @Autowired
    private PaymentTransactionRepository repository;

    @Test
    void findAll_shouldReturnAllSavedTransactions() {
        PaymentTransaction tx1 = PaymentTransaction.builder()
                .userId(UUID.randomUUID())
                .amount(10.0)
                .type("TOPUP")
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();
        PaymentTransaction tx2 = PaymentTransaction.builder()
                .userId(UUID.randomUUID())
                .amount(20.0)
                .type("PURCHASE")
                .status("FAILED")
                .createdAt(Instant.now())
                .build();
        repository.save(tx1);
        repository.save(tx2);

        List<PaymentTransaction> all = repository.findAll();

        assertThat(all)
                .hasSize(2)
                .extracting(PaymentTransaction::getStatus)
                .containsExactlyInAnyOrder("SUCCESS", "FAILED");
    }

    @Test
    void findByStatusNot_shouldExcludeSoftDeleted() {
        PaymentTransaction active = PaymentTransaction.builder()
                .userId(UUID.randomUUID())
                .amount(30.0)
                .type("PURCHASE")
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();
        PaymentTransaction deleted = PaymentTransaction.builder()
                .userId(UUID.randomUUID())
                .amount(40.0)
                .type("TOPUP")
                .status("SOFT_DELETED")
                .createdAt(Instant.now())
                .build();
        repository.save(active);
        repository.save(deleted);

        List<PaymentTransaction> result = repository.findByStatusNot("SOFT_DELETED");

        assertThat(result)
                .hasSize(1)
                .extracting(PaymentTransaction::getStatus)
                .containsExactly("SUCCESS");
    }
}