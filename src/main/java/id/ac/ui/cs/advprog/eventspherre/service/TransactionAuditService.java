package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;

import java.util.List;
import java.util.UUID;

public interface TransactionAuditService {
    List<PaymentTransaction> getActive();       // hide soft‑deleted
    List<PaymentTransaction> getAll();          // full audit
    void flagFailed(UUID id);
    void softDelete(UUID id);
    void hardDelete  (UUID id);
    void markSuccess(UUID id);
}
