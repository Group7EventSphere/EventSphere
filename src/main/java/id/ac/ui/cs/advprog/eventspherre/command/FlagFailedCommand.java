package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Marks a transaction as FAILED.
 */
@RequiredArgsConstructor
public class FlagFailedCommand implements AuditCommand {
    private final UUID transactionId;
    private final PaymentTransactionRepository repo;

    @Override
    public void execute() {
        repo.findById(transactionId)
            .ifPresent(tx -> {
                tx.setStatus("FAILED");
            });
    }
}