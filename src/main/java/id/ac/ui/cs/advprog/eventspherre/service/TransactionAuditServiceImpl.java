package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.command.AuditCommandInvoker;
import id.ac.ui.cs.advprog.eventspherre.command.FlagFailedCommand;
import id.ac.ui.cs.advprog.eventspherre.command.SoftDeleteCommand;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionAuditServiceImpl implements TransactionAuditService {

    private final PaymentTransactionRepository repo;
    private final AuditCommandInvoker invoker;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getActive() {
        return repo.findByStatusNot("SOFT_DELETED");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getAll() {
        return repo.findAll();
    }

    @Override
    @Transactional
    public void flagFailed(UUID id) {
        invoker.invoke(new FlagFailedCommand(id, repo));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        invoker.invoke(new SoftDeleteCommand(id, repo));
    }
}
