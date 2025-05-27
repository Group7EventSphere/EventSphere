package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentTransactionRepositoryTest {

    @Autowired
    private PaymentTransactionRepository repo;

    @Test
    void findByStatusNot_excludesSoftDeleted() {
        PaymentTransaction tx1 = new PaymentTransaction();
        tx1.setStatus("COMPLETED");
        tx1.setCreatedAt(Instant.now());
        tx1.setUserId(1);
        tx1.setAmount(100.0);
        tx1.setType(PaymentRequest.PaymentType.TOPUP);
        tx1.setRequestId(UUID.randomUUID());
        repo.saveAndFlush(tx1);

        PaymentTransaction tx2 = new PaymentTransaction();
        tx2.setStatus("SOFT_DELETED");
        tx2.setCreatedAt(Instant.now());
        tx2.setUserId(2);
        tx2.setAmount(50.0);
        tx2.setType(PaymentRequest.PaymentType.PURCHASE);
        tx2.setRequestId(UUID.randomUUID());
        repo.saveAndFlush(tx2);

        List<PaymentTransaction> result = repo.findByStatusNot("SOFT_DELETED");

        assertThat(result)
            .hasSize(1)
            .extracting(PaymentTransaction::getStatus)
            .containsExactly("COMPLETED");
    }
}
