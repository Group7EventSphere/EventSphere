package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketTypeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class FlagFailedCommand implements AuditCommand {

    private final UUID                         txId;
    private final PaymentTransactionRepository txRepo;
    private final PaymentRequestRepository     reqRepo;
    private final UserRepository               userRepo;
    private final TicketRepository             ticketRepo;
    private final TicketTypeRepository         ticketTypeRepo;

    @Override @Transactional
    public void execute() {
        txRepo.findById(txId).ifPresent(tx -> {
            tx.setStatus("FAILED");

            // fetch exact originating request via FK
            reqRepo.findById(tx.getRequestId()).ifPresent(req -> {
                req.setAmount(-req.getAmount());
                req.setProcessed(false);
                req.setMessage("ADMIN-FLAG: FAILED");
                reqRepo.save(req);
            });

            userRepo.findById(tx.getUserId()).ifPresent(u -> {
                if (tx.getType() == PaymentRequest.PaymentType.TOPUP)
                    u.deduct(tx.getAmount());   // cancel a top-up
                else {
                    u.topUp(tx.getAmount());    // refund a purchase
                    
                    // If this was a purchase, restore ticket quota
                    if (tx.getType() == PaymentRequest.PaymentType.PURCHASE) {
                        List<Ticket> tickets = ticketRepo.findByTransactionId(tx.getId());
                        if (!tickets.isEmpty()) {
                            // Group tickets by ticket type and restore quota
                            tickets.stream()
                                .collect(java.util.stream.Collectors.groupingBy(
                                    t -> t.getTicketType().getId(),
                                    java.util.stream.Collectors.counting()
                                ))
                                .forEach((ticketTypeId, count) -> 
                                    ticketTypeRepo.findById(ticketTypeId).ifPresent(ticketType -> {
                                        ticketType.setQuota(ticketType.getQuota() + count.intValue());
                                        ticketTypeRepo.save(ticketType);
                                    })
                                );
                            
                            // Delete the tickets
                            ticketRepo.deleteByTransactionId(tx.getId());
                        }
                    }
                }
            });
        });
    }
}
