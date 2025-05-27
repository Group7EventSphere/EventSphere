package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.command.*;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionAuditServiceImplTest {

    @Mock
    private PaymentTransactionRepository txRepo;

    @Mock
    private PaymentRequestRepository reqRepo;

    @Mock
    private UserRepository userRepo;

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
        when(txRepo.findByStatusNot("SOFT_DELETED")).thenReturn(List.of(tx1));

        var result = service.getActive();

        assertThat(result).containsExactly(tx1);
        verify(txRepo).findByStatusNot("SOFT_DELETED");
    }

    @Test
    void getAll_shouldCallRepositoryFindAll() {
        PaymentTransaction tx1 = mock(PaymentTransaction.class);
        when(txRepo.findAll()).thenReturn(List.of(tx1));

        var result = service.getAll();

        assertThat(result).containsExactly(tx1);
        verify(txRepo).findAll();
    }

    @Test
    void flagFailed_shouldInvokeFlagFailedCommand() throws Exception {
        UUID id = UUID.randomUUID();

        service.flagFailed(id);

        // capture the exact command passed into the invoker
        ArgumentCaptor<FlagFailedCommand> captor =
            ArgumentCaptor.forClass(FlagFailedCommand.class);
        verify(invoker).invoke(captor.capture());

        // now reflectively get the private txId field
        FlagFailedCommand cmd = captor.getValue();
        Field txIdField = FlagFailedCommand.class
            .getDeclaredField("txId");
        txIdField.setAccessible(true);

        assertThat(txIdField.get(cmd))
            .isEqualTo(id);
    }
    
    @Test
    void markSuccessful_shouldInvokeMarkSuccessCommand() throws Exception {
        UUID id = UUID.randomUUID();
        service.markSuccess(id);

        ArgumentCaptor<MarkSuccessCommand> captor = ArgumentCaptor.forClass(MarkSuccessCommand.class);
        verify(invoker).invoke(captor.capture());

        MarkSuccessCommand cmd = captor.getValue();
        Field txIdField = MarkSuccessCommand.class.getDeclaredField("txId");
        txIdField.setAccessible(true);
        assertThat(txIdField.get(cmd)).isEqualTo(id);
    }

    @Test
    void softDelete_shouldInvokeSoftDeleteCommand() throws Exception {
        UUID id = UUID.randomUUID();

        service.softDelete(id);

        ArgumentCaptor<SoftDeleteCommand> captor =
            ArgumentCaptor.forClass(SoftDeleteCommand.class);
        verify(invoker).invoke(captor.capture());

        SoftDeleteCommand cmd = captor.getValue();
        Field txIdField = SoftDeleteCommand.class
            .getDeclaredField("txId");
        txIdField.setAccessible(true);

        assertThat(txIdField.get(cmd))
            .isEqualTo(id);
    }

    @Test
    void hardDelete_shouldInvokeHardDeleteCommand() throws Exception {
        UUID id = UUID.randomUUID();

        service.hardDelete(id);

        ArgumentCaptor<HardDeleteCommand> captor =
            ArgumentCaptor.forClass(HardDeleteCommand.class);
        verify(invoker).invoke(captor.capture());

        HardDeleteCommand cmd = captor.getValue();
        Field txIdField = HardDeleteCommand.class
            .getDeclaredField("txId");
        txIdField.setAccessible(true);

        assertThat(txIdField.get(cmd))
            .isEqualTo(id);
    }

}