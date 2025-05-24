package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class MarkSuccessCommand implements AuditCommand {

    private final UUID                         txId;
    private final PaymentTransactionRepository txRepo;
    private final PaymentRequestRepository     reqRepo;
    private final UserRepository               userRepo;

    @Override @Transactional
    public void execute() {

        txRepo.findById(txId).ifPresent(tx -> {

            reqRepo.findById(tx.getRequestId()).ifPresent(req -> {

                boolean needReApply = !req.isProcessed(); 

                req.setProcessed(true);
                req.setAmount(Math.abs(req.getAmount())); 
                req.setMessage("ADMIN-MARK: SUCCESS");
                reqRepo.save(req);

                if (needReApply) {
                    userRepo.findById(tx.getUserId()).ifPresent(u -> {
                        if (tx.getType() == PaymentRequest.PaymentType.TOPUP)
                            u.topUp(tx.getAmount());    
                        else
                            u.deduct(tx.getAmount());  
                    });
                }
            });

            tx.setStatus("SUCCESS");
        });
    }
}
