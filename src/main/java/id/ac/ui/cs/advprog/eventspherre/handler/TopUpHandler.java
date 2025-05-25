package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TopUpHandler implements PaymentHandler {

    private final UserRepository userRepo;
    private final PaymentRequestRepository requestRepo;
    private PaymentHandler next;

    @Override
    public void setNext(PaymentHandler handler) {
        this.next = handler;
    }

    @Override
    @Transactional
    public void handle(PaymentRequest request) {
        if (request.getPaymentType() == PaymentRequest.PaymentType.TOPUP && request.getAmount() > AppConstants.MIN_VALID_ID) {
            User user = userRepo.findById(request.getUser().getId()).orElseThrow();
            user.topUp(request.getAmount());
            request.setProcessed(true);
            request.setMessage("Top-up successful");
            requestRepo.save(request);
            return;
        }
        if (next != null) next.handle(request);
    }
}
