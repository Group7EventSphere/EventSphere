package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest.PaymentType;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class PaymentServiceTest {

    @Mock
    private PaymentRequestRepository requestRepo;

    @Mock
    private PaymentTransactionRepository txRepo;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void persistRequestAndConvert_savesRequestAndTransaction() {
        User user = new User();
        user.setId(42);

        double amount = 123.45;
        Instant created = Instant.parse("2025-05-19T12:00:00Z");

        PaymentRequest req = new PaymentRequest(user, amount, PaymentType.TOPUP);
        req.setCreatedAt(created);
        req.setProcessed(true);
        req.setMessage("initial");

        given(requestRepo.save(req)).willReturn(req);

        given(txRepo.save(any(PaymentTransaction.class)))
            .willAnswer(inv -> inv.getArgument(0));

        PaymentTransaction tx = paymentService.persistRequestAndConvert(req, "SUCCESS");

        assertThat(tx.getUserId()).isEqualTo(42);
        assertThat(tx.getAmount()).isEqualTo(amount);
        assertThat(tx.getType()).isEqualTo(PaymentType.TOPUP);
        assertThat(tx.getStatus()).isEqualTo("SUCCESS");
        assertThat(tx.getCreatedAt()).isEqualTo(created);
    }
}
