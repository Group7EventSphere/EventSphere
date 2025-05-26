package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;

public class SufficientBalanceHandler implements PaymentHandler {
    private PaymentHandler next;

    @Override
    public void setNext(PaymentHandler handler) {
        this.next = handler;
    }

    @Override
    public void handle(PaymentRequest request) {
        if (request.getPaymentType() == PaymentRequest.PaymentType.PURCHASE) {
            User user = request.getUser();
            if (user.getBalance() < request.getAmount()) {
                request.setProcessed(false);
                request.setMessage("Payment failed: insufficient funds");
                return; // Stop the chain if funds are insufficient.
            }
        }
        if (next != null) {
            next.handle(request);
        }
    }
}
