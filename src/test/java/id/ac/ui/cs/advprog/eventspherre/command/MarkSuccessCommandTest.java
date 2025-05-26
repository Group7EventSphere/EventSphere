package id.ac.ui.cs.advprog.eventspherre.command;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentTransactionRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MarkSuccessCommandTest {

    @Mock
    private PaymentTransactionRepository txRepo;

    @Mock
    private PaymentRequestRepository reqRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private PaymentTransaction mockTransaction;

    @Mock
    private PaymentRequest mockRequest;

    @Mock
    private User mockUser;

    private UUID txId;
    private UUID reqId;
    private Integer userId;
    private MarkSuccessCommand command;

    @BeforeEach
    void setUp() {
        txId = UUID.randomUUID();
        reqId = UUID.randomUUID();
        userId = 123;
        command = new MarkSuccessCommand(txId, txRepo, reqRepo, userRepo);
    }

    @Test
    void testExecute_TransactionNotFound() {
        // Arrange
        when(txRepo.findById(txId)).thenReturn(Optional.empty());

        // Act
        command.execute();

        // Assert
        verify(txRepo).findById(txId);
        verify(reqRepo, never()).findById(any());
        verify(userRepo, never()).findById(any());
    }

    @Test
    void testExecute_RequestNotFound() {
        // Arrange
        when(txRepo.findById(txId)).thenReturn(Optional.of(mockTransaction));
        when(mockTransaction.getRequestId()).thenReturn(reqId);
        when(reqRepo.findById(reqId)).thenReturn(Optional.empty());

        // Act
        command.execute();

        // Assert
        verify(txRepo).findById(txId);
        verify(reqRepo).findById(reqId);
        verify(mockTransaction).setStatus("SUCCESS");
        verify(userRepo, never()).findById(any());
    }

    @Test
    void testExecute_RequestAlreadyProcessed() {
        // Arrange
        when(txRepo.findById(txId)).thenReturn(Optional.of(mockTransaction));
        when(mockTransaction.getRequestId()).thenReturn(reqId);
        when(reqRepo.findById(reqId)).thenReturn(Optional.of(mockRequest));
        when(mockRequest.isProcessed()).thenReturn(true);

        // Act
        command.execute();

        // Assert
        verify(txRepo).findById(txId);
        verify(reqRepo).findById(reqId);
        verify(mockRequest).setProcessed(true);
        verify(mockRequest).setAmount(anyDouble());
        verify(mockRequest).setMessage("ADMIN-MARK: SUCCESS");
        verify(reqRepo).save(mockRequest);
        verify(mockTransaction).setStatus("SUCCESS");
        verify(userRepo, never()).findById(any());
    }

    @Test
    void testExecute_TopUpTransactionSuccess() {
        // Arrange
        when(txRepo.findById(txId)).thenReturn(Optional.of(mockTransaction));
        when(mockTransaction.getRequestId()).thenReturn(reqId);
        when(mockTransaction.getUserId()).thenReturn(userId);
        when(mockTransaction.getAmount()).thenReturn(100.0);
        when(mockTransaction.getType()).thenReturn(PaymentRequest.PaymentType.TOPUP);
        
        when(reqRepo.findById(reqId)).thenReturn(Optional.of(mockRequest));
        when(mockRequest.isProcessed()).thenReturn(false);
        when(mockRequest.getAmount()).thenReturn(-100.0);
        
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        command.execute();

        // Assert
        verify(txRepo).findById(txId);
        verify(reqRepo).findById(reqId);
        verify(mockRequest).setProcessed(true);
        verify(mockRequest).setAmount(100.0); // Math.abs(-100.0)
        verify(mockRequest).setMessage("ADMIN-MARK: SUCCESS");
        verify(reqRepo).save(mockRequest);
        verify(userRepo).findById(userId);
        verify(mockUser).topUp(100.0);
        verify(mockTransaction).setStatus("SUCCESS");
    }

    @Test
    void testExecute_PaymentTransactionSuccess() {
        // Arrange
        when(txRepo.findById(txId)).thenReturn(Optional.of(mockTransaction));
        when(mockTransaction.getRequestId()).thenReturn(reqId);
        when(mockTransaction.getUserId()).thenReturn(userId);
        when(mockTransaction.getAmount()).thenReturn(50.0);
        when(mockTransaction.getType()).thenReturn(PaymentRequest.PaymentType.PURCHASE);
        
        when(reqRepo.findById(reqId)).thenReturn(Optional.of(mockRequest));
        when(mockRequest.isProcessed()).thenReturn(false);
        when(mockRequest.getAmount()).thenReturn(-50.0);
        
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        command.execute();

        // Assert
        verify(txRepo).findById(txId);
        verify(reqRepo).findById(reqId);
        verify(mockRequest).setProcessed(true);
        verify(mockRequest).setAmount(50.0); // Math.abs(-50.0)
        verify(mockRequest).setMessage("ADMIN-MARK: SUCCESS");
        verify(reqRepo).save(mockRequest);
        verify(userRepo).findById(userId);
        verify(mockUser).deduct(50.0);
        verify(mockTransaction).setStatus("SUCCESS");
    }    @Test
    void testExecute_UserNotFound() {
        // Arrange
        when(txRepo.findById(txId)).thenReturn(Optional.of(mockTransaction));
        when(mockTransaction.getRequestId()).thenReturn(reqId);
        when(mockTransaction.getUserId()).thenReturn(userId);
        
        when(reqRepo.findById(reqId)).thenReturn(Optional.of(mockRequest));
        when(mockRequest.isProcessed()).thenReturn(false);
        when(mockRequest.getAmount()).thenReturn(-100.0);
        
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        // Act
        command.execute();

        // Assert
        verify(txRepo).findById(txId);
        verify(reqRepo).findById(reqId);
        verify(mockRequest).setProcessed(true);
        verify(mockRequest).setAmount(100.0); // Math.abs(-100.0)
        verify(mockRequest).setMessage("ADMIN-MARK: SUCCESS");
        verify(reqRepo).save(mockRequest);
        verify(userRepo).findById(userId);
        // User methods should never be called since user is not found
        verify(mockTransaction).setStatus("SUCCESS");
    }
}
