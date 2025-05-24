package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.command.AuditCommandInvoker;
import id.ac.ui.cs.advprog.eventspherre.command.FlagFailedCommand;
import id.ac.ui.cs.advprog.eventspherre.command.HardDeleteCommand;
import id.ac.ui.cs.advprog.eventspherre.command.MarkSuccessCommand;
import id.ac.ui.cs.advprog.eventspherre.command.SoftDeleteCommand;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionAuditServiceImpl implements TransactionAuditService {

    private final PaymentTransactionRepository txRepo;
    private final PaymentRequestRepository     reqRepo;
    private final UserRepository               userRepo;
    private final TicketRepository             ticketRepo;
    private final TicketTypeRepository         ticketTypeRepo;
    private final AuditCommandInvoker          invoker;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getActive() {
        return txRepo.findByStatusNot("SOFT_DELETED");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getAll() {
        return txRepo.findAll();
    }

    @Override
    @Transactional
    public void flagFailed(UUID id) {
        invoker.invoke(new FlagFailedCommand(id, txRepo, reqRepo, userRepo, ticketRepo, ticketTypeRepo));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        invoker.invoke(new SoftDeleteCommand(id, txRepo, reqRepo, userRepo, ticketRepo, ticketTypeRepo));
    }

    @Override
    @Transactional
    public void hardDelete(UUID id) {
        invoker.invoke(new HardDeleteCommand(id, txRepo, reqRepo, userRepo, ticketRepo, ticketTypeRepo));
    }

    @Override @Transactional
    public void markSuccess(UUID id) {
        invoker.invoke(new MarkSuccessCommand(id, txRepo, reqRepo, userRepo));
    }
}
