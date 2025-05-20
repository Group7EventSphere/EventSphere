package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Marks a transaction as SOFT_DELETED.
 */
@RequiredArgsConstructor
public class SoftDeleteCommand implements AuditCommand {
    private final UUID transactionId;
    private final PaymentTransactionRepository repo;

    @Override
    public void execute() {
        repo.findById(transactionId)
            .ifPresent(tx -> {
                tx.setStatus("SOFT_DELETED");
            });
    }
}
