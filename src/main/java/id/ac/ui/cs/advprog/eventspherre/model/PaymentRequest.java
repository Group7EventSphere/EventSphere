package id.ac.ui.cs.advprog.eventspherre.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    public enum PaymentType {
        TOPUP, PURCHASE
    }

    private final User user;
    private final double amount;
    private final PaymentType paymentType;
    private boolean processed = false;
    private String message;

    public PaymentRequest(User user, double amount, PaymentType paymentType) {
        this.user = user;
        this.amount = amount;
        this.paymentType = paymentType;
    }
}
