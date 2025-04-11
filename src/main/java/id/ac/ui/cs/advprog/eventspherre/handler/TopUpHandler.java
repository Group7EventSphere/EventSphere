package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;

public class TopUpHandler implements PaymentHandler {
    private PaymentHandler next;

    @Override
    public void setNext(PaymentHandler handler) {
        this.next = handler;
    }

    @Override
    public void handle(PaymentRequest request) {
        if (request.getPaymentType() == PaymentRequest.PaymentType.TOPUP) {
            User user = request.getUser();
            user.topUp(request.getAmount());
            request.setProcessed(true);
            request.setMessage("Top-up successful: balance added");
            return;
        }
        if (next != null) {
            next.handle(request);
        }
    }
}
