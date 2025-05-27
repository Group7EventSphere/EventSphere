package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketTypeRepository;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class HardDeleteCommand implements AuditCommand {

    private final UUID                         txId;
    private final PaymentTransactionRepository txRepo;
    private final PaymentRequestRepository     reqRepo;
    private final UserRepository               userRepo;
    private final TicketRepository             ticketRepo;
    private final TicketTypeRepository         ticketTypeRepo;

    @Override @Transactional
    public void execute() {
        txRepo.findById(txId).ifPresent(tx -> {
            updateRequestMessage(tx.getRequestId());
            
            boolean alreadyFinal = isTransactionFinal(tx);
            if (!alreadyFinal) {
                processUserBalanceAndTickets(tx);
            }
            
            deleteTransactionAndRequest(tx);
        });
    }

    private void updateRequestMessage(UUID requestId) {
        reqRepo.findById(requestId).ifPresent(req -> {
            req.setMessage("ADMIN-DELETE: HARD");
            reqRepo.save(req);
        });
    }

    private boolean isTransactionFinal(PaymentTransaction tx) {
        return "FAILED".equals(tx.getStatus()) || "SOFT_DELETED".equals(tx.getStatus());
    }

    private void processUserBalanceAndTickets(PaymentTransaction tx) {
        userRepo.findById(tx.getUserId()).ifPresent(user -> {
            if (tx.getType() == PaymentRequest.PaymentType.TOPUP) {
                user.deduct(tx.getAmount());
            } else {
                handlePurchaseRefund(tx, user);
            }
        });
    }

    private void handlePurchaseRefund(PaymentTransaction tx, User user) {
        user.topUp(tx.getAmount());
        
        if (tx.getType() == PaymentRequest.PaymentType.PURCHASE) {
            restoreTicketQuota(tx.getId());
        }
    }

        private void restoreTicketQuota(UUID transactionId) {
        List<Ticket> tickets = ticketRepo.findByTransactionId(transactionId);
        if (tickets.isEmpty()) {
            return;
        }
        
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
        
        ticketRepo.deleteByTransactionId(transactionId);
    }

    private void deleteTransactionAndRequest(PaymentTransaction tx) {
        UUID reqId = tx.getRequestId();
        txRepo.delete(tx);
        reqRepo.findById(reqId).ifPresent(reqRepo::delete);
    }
}