package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.command.AuditCommandInvoker;
import id.ac.ui.cs.advprog.eventspherre.command.FlagFailedCommand;
import id.ac.ui.cs.advprog.eventspherre.command.SoftDeleteCommand;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionAuditServiceImplTest {

    @Mock
    private PaymentTransactionRepository repo;

    @Mock
    private AuditCommandInvoker invoker;

    @InjectMocks
    private TransactionAuditServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getActive_shouldCallRepositoryWithSoftDeletedExcluded() {
        PaymentTransaction tx1 = mock(PaymentTransaction.class);
        when(repo.findByStatusNot("SOFT_DELETED")).thenReturn(List.of(tx1));

        List<PaymentTransaction> result = service.getActive();

        assertThat(result).containsExactly(tx1);
        verify(repo).findByStatusNot("SOFT_DELETED");
    }

    @Test
    void getAll_shouldCallRepositoryFindAll() {
        PaymentTransaction tx1 = mock(PaymentTransaction.class);
        when(repo.findAll()).thenReturn(List.of(tx1));

        List<PaymentTransaction> result = service.getAll();

        assertThat(result).containsExactly(tx1);
        verify(repo).findAll();
    }

    @Test
    void flagFailed_shouldInvokeFlagFailedCommand() {
        UUID id = UUID.randomUUID();
        service.flagFailed(id);

        ArgumentCaptor<FlagFailedCommand> captor = ArgumentCaptor.forClass(FlagFailedCommand.class);
        verify(invoker).invoke(captor.capture());
        assertThat(captor.getValue()).extracting("transactionId").isEqualTo(id);
    }

    @Test
    void softDelete_shouldInvokeSoftDeleteCommand() {
        UUID id = UUID.randomUUID();
        service.softDelete(id);

        ArgumentCaptor<SoftDeleteCommand> captor = ArgumentCaptor.forClass(SoftDeleteCommand.class);
        verify(invoker).invoke(captor.capture());
        assertThat(captor.getValue()).extracting("transactionId").isEqualTo(id);
    }
}
