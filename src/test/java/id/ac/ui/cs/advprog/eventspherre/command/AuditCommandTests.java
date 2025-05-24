package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditCommandTests {

    @Mock private PaymentTransactionRepository txRepo;
    @Mock private PaymentRequestRepository     reqRepo;
    @Mock private UserRepository               usrRepo;
    @Mock private PaymentTransaction           tx;
    @Mock private PaymentRequest               req;
    @Mock private User                         usr;

    private final UUID txId  = UUID.randomUUID();
    private final UUID reqId = UUID.randomUUID();


    private void commonStubbing(double amount,
                                int userId,
                                PaymentRequest.PaymentType type)
    {
        when(tx.getRequestId()).thenReturn(reqId);
        when(tx.getUserId()).thenReturn(userId);
        when(tx.getAmount()).thenReturn(amount);
        when(tx.getType()).thenReturn(type);
        when(txRepo.findById(txId)).thenReturn(Optional.of(tx));
        when(reqRepo.findById(reqId)).thenReturn(Optional.of(req));
        when(usrRepo.findById(userId)).thenReturn(Optional.of(usr));
    }


    @Nested
    @DisplayName("FlagFailedCommand")
    class FlagFailed {

        @Test
        @DisplayName("sets FAILED, rolls back request & deducts user (TOPUP)")
        void shouldFlagFailedAndRevertTopup() {
            commonStubbing(150.0, 1, PaymentRequest.PaymentType.TOPUP);

            new FlagFailedCommand(txId, txRepo, reqRepo, usrRepo).execute();

            verify(tx).setStatus("FAILED");
            req.setAmount(-tx.getAmount());
            verify(req).setProcessed(false);
            verify(req).setMessage("ADMIN-FLAG: FAILED");
            verify(reqRepo).save(req);
            verify(usr).deduct(150.0);
        }

        @Test
        @DisplayName("refunds user when purchase failed")
        void shouldRefundPurchase() {
            commonStubbing(80.0, 2, PaymentRequest.PaymentType.PURCHASE);

            new FlagFailedCommand(txId, txRepo, reqRepo, usrRepo).execute();

            verify(tx).setStatus("FAILED");
            req.setAmount(-tx.getAmount());
            verify(reqRepo).save(req);
            verify(usr).topUp(80.0);
        }
    }


    @Nested
    @DisplayName("HardDeleteCommand")
    class HardDelete {

        @Test
        @DisplayName("deletes tx & request, reverses user balance")
        void shouldDeleteBothEntities() {
            commonStubbing(50.0, 3, PaymentRequest.PaymentType.TOPUP);

            new HardDeleteCommand(txId, txRepo, reqRepo, usrRepo).execute();

            verify(usr).deduct(50.0);
            verify(txRepo).delete(tx);
            verify(reqRepo).delete(req);
        }
    }


    @Nested
    @DisplayName("SoftDeleteCommand")
    class SoftDelete {

        @Test
        @DisplayName("sets SOFT_DELETED, marks request & deducts user")
        void shouldSoftDelete() {
            commonStubbing(30.0, 4, PaymentRequest.PaymentType.TOPUP);

            new SoftDeleteCommand(txId, txRepo, reqRepo, usrRepo).execute();

            verify(tx).setStatus("SOFT_DELETED");
            req.setAmount(-tx.getAmount());
            verify(req).setProcessed(false);
            verify(req).setMessage("ADMIN-DELETE: SOFT_DELETED");
            verify(reqRepo).save(req);
            verify(usr).deduct(30.0);
        }
    }


    @Test
    @DisplayName("AuditCommandInvoker calls execute() exactly once")
    void invokerShouldInvoke() {
        AuditCommand mockCmd = mock(AuditCommand.class);
        AuditCommandInvoker invoker = new AuditCommandInvoker();

        invoker.invoke(mockCmd);

        verify(mockCmd, times(1)).execute();
    }
}
