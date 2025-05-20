package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.User;

public class TopUpHandler implements PaymentHandler {
    private PaymentHandler next;

    @Override
    public void setNext(PaymentHandler handler) {
        this.next = handler;
    }

    @Override
    public void handle(PaymentRequest request) {
        if (request.getPaymentType() == PaymentRequest.PaymentType.TOPUP
            && request.getAmount() > 0)
        {
            User user = request.getUser();
            // serialize all concurrent top-ups
            synchronized(user) {
                user.topUp(request.getAmount());
                request.setProcessed(true);
                request.setMessage("Top-up successful: balance added");
            }
            return;
        }
        if (next != null) {
            next.handle(request);
        }
    }
}
