package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;

public class DeductBalanceHandler implements PaymentHandler {
    private PaymentHandler next;

    @Override
    public void setNext(PaymentHandler handler) {
        this.next = handler;
    }

    @Override
    public void handle(PaymentRequest request) {
        if (request.getPaymentType() == PaymentRequest.PaymentType.PURCHASE
            && request.getAmount() > AppConstants.MIN_VALID_ID) 
        {
            User user = request.getUser();
            synchronized(user) {
                double balance = user.getBalance();
                if (balance >= request.getAmount()) {
                    user.deduct(request.getAmount());
                    request.setProcessed(true);
                    request.setMessage("Purchase successful: balance deducted");
                } else {
                    request.setProcessed(false);
                    request.setMessage("Payment failed: insufficient funds");

                }
            }
        }
        if (next != null) {
            next.handle(request);
        }
    }
}
