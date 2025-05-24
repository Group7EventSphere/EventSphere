package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
public class SoftDeleteCommand implements AuditCommand {

    private final UUID                         txId;
    private final PaymentTransactionRepository txRepo;
    private final PaymentRequestRepository     reqRepo;
    private final UserRepository               userRepo;

    @Override @Transactional
    public void execute() {
        txRepo.findById(txId).ifPresent(tx -> {
            tx.setStatus("SOFT_DELETED");

            reqRepo.findById(tx.getRequestId()).ifPresent(req -> {
                req.setAmount(-req.getAmount());
                req.setProcessed(false);
                req.setMessage("ADMIN-DELETE: SOFT_DELETED");
                reqRepo.save(req);
            });

            userRepo.findById(tx.getUserId()).ifPresent(u -> {
                if (tx.getType() == PaymentRequest.PaymentType.TOPUP)
                    u.deduct(tx.getAmount());
                else
                    u.topUp(tx.getAmount());
            });
        });
    }
}
