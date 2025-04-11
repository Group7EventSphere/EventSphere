package id.ac.ui.cs.advprog.eventspherre.model;

public class PaymentRequest {
    public enum PaymentType {
        TOPUP, PURCHASE
    }

    private User user;
    private double amount;
    private PaymentType paymentType;
    private boolean processed = false;
    private String message;

    public PaymentRequest(User user, double amount, PaymentType paymentType) {
        this.user = user;
        this.amount = amount;
        this.paymentType = paymentType;
    }

    public User getUser() {
        return user;
    }
    public double getAmount() {
        return amount;
    }
    public PaymentType getPaymentType() {
        return paymentType;
    }

    public boolean isProcessed() {
        return processed;
    }
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
