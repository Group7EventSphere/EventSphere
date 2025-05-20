package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRequestRepository requestRepo;
    private final PaymentTransactionRepository txRepo;

    @Transactional
    public PaymentTransaction persistRequestAndConvert(PaymentRequest req, String status) {
        PaymentRequest saved = requestRepo.save(req);
        PaymentTransaction tx = PaymentTransaction.builder()
                .userId(saved.getUserId())
                .amount(saved.getAmount())
                .type(saved.getType())
                .status(status)
                .createdAt(saved.getCreatedAt())
                .build();
        return txRepo.save(tx);
    }
}
