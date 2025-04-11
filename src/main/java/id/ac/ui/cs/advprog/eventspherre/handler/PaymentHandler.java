package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;

public interface PaymentHandler {
    void setNext(PaymentHandler handler);
    void handle(PaymentRequest request);
}
