package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.User;

public class DeductBalanceHandler implements PaymentHandler {
    private PaymentHandler next;

    @Override
    public void setNext(PaymentHandler handler) {
        this.next = handler;
    }

    @Override
    public void handle(PaymentRequest request) {
        if (request.getPaymentType() == PaymentRequest.PaymentType.PURCHASE) {
            User user = request.getUser();

            // Order of the chain determines that balance should be checked before this stage
            if (user.getBalance() >= request.getAmount()) {
                user.deduct(request.getAmount());
                request.setProcessed(true);
                request.setMessage("Purchase successful: balance deducted");
            }
        }
        if (next != null) {
            next.handle(request);
        }
    }
}
