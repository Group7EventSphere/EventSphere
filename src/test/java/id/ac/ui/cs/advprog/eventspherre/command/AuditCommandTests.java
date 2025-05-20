package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class AuditCommandTests {

    @Test
    void flagFailedCommand_shouldSetStatusToFailed() {
        UUID id = UUID.randomUUID();
        PaymentTransaction tx = mock(PaymentTransaction.class);
        PaymentTransactionRepository repo = mock(PaymentTransactionRepository.class);
        when(repo.findById(id)).thenReturn(Optional.of(tx));

        FlagFailedCommand cmd = new FlagFailedCommand(id, repo);
        cmd.execute();

        verify(tx).setStatus("FAILED");
    }

    @Test
    void softDeleteCommand_shouldSetStatusToSoftDeleted() {
        UUID id = UUID.randomUUID();
        PaymentTransaction tx = mock(PaymentTransaction.class);
        PaymentTransactionRepository repo = mock(PaymentTransactionRepository.class);
        when(repo.findById(id)).thenReturn(Optional.of(tx));

        SoftDeleteCommand cmd = new SoftDeleteCommand(id, repo);
        cmd.execute();

        verify(tx).setStatus("SOFT_DELETED");
    }
}