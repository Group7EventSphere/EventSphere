package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest.PaymentType;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRequestRepositoryTest {

    @Autowired
    private PaymentRequestRepository repo;

    @Test
    void findByUserId_returnsRequestsForThatUser() {
        PaymentRequest r1 = new PaymentRequest();
        r1.setUserId(1);
        r1.setAmount(100.0);
        r1.setType(PaymentType.TOPUP);
        r1.setProcessed(false);
        r1.setCreatedAt(Instant.now());
        r1 = repo.saveAndFlush(r1);

        PaymentRequest r2 = new PaymentRequest();
        r2.setUserId(2);
        r2.setAmount(200.0);
        r2.setType(PaymentType.PURCHASE);
        r2.setProcessed(false);
        r2.setCreatedAt(Instant.now());
        repo.saveAndFlush(r2);

        PaymentRequest r3 = new PaymentRequest();
        r3.setUserId(1);
        r3.setAmount(300.0);
        r3.setType(PaymentType.PURCHASE);
        r3.setProcessed(false);
        r3.setCreatedAt(Instant.now());
        r3 = repo.saveAndFlush(r3);

        List<PaymentRequest> results = repo.findByUserId(1);

        assertThat(results)
            .hasSize(2)
            .extracting(PaymentRequest::getUserId)
            .containsOnly(1);
    }

    @Test
    void findByUserId_returnsEmptyWhenNoMatch() {
        List<PaymentRequest> results = repo.findByUserId(999);
        assertThat(results).isEmpty();
    }
}
