package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class HardDeleteCommand implements AuditCommand {

    private final UUID                         txId;
    private final PaymentTransactionRepository txRepo;
    private final PaymentRequestRepository     reqRepo;
    private final UserRepository               userRepo;

    @Override @Transactional
    public void execute() {

        txRepo.findById(txId).ifPresent(tx -> {

            userRepo.findById(tx.getUserId()).ifPresent(u -> {
                if (tx.getType() == PaymentRequest.PaymentType.TOPUP)
                    u.deduct(tx.getAmount());
                else
                    u.topUp(tx.getAmount());
            });

            UUID reqId = tx.getRequestId();   // capture before deleting

            txRepo.delete(tx);

            reqRepo.findById(reqId).ifPresent(reqRepo::delete);
        });
    }
}
