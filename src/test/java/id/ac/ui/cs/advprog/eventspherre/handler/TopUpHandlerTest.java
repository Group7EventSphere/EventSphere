package id.ac.ui.cs.advprog.eventspherre.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest.PaymentType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class TopUpHandlerTest {

    private UserRepository              userRepo;
    private PaymentRequestRepository    requestRepo;
    private TopUpHandler                handler;

    @BeforeEach
    void setUp() {
        userRepo    = mock(UserRepository.class);
        requestRepo = mock(PaymentRequestRepository.class);
        handler     = new TopUpHandler(userRepo, requestRepo);
    }

    @Test
    void handle_withValidTopUp_updatesBalance_marksProcessed_andSaves() {
        User user = new User();
        user.setId(1);
        user.setBalance(100.0);

        // Create a TOPUP request for 50.0
        PaymentRequest req = new PaymentRequest();
        req.setUser(user);
        req.setPaymentType(PaymentType.TOPUP);
        req.setAmount(50.0);

        // Stub repository lookup
        when(userRepo.findById(1)).thenReturn(Optional.of(user));

        handler.handle(req);

        // Assert
        // - user balance increased
        assertEquals(150.0, user.getBalance(), 0.0001);
        // - request marked processed
        assertTrue(req.isProcessed());
        // - message set
        assertEquals("Top-up successful", req.getMessage());
        // - saved exactly once
        verify(requestRepo, times(1)).save(req);
    }

    @Test
    void handle_withTopUpButZeroOrNegativeAmount_delegatesToNext() {
        PaymentRequest req = new PaymentRequest();
        req.setPaymentType(PaymentType.TOPUP);
        req.setAmount(0.0);

        PaymentHandler next = mock(PaymentHandler.class);
        handler.setNext(next);

        handler.handle(req);

        verify(next, times(1)).handle(req);
        verifyNoInteractions(userRepo);
        verifyNoInteractions(requestRepo);
    }

    @Test
    void handle_withNonTopUp_delegatesToNext() {
        PaymentRequest req = new PaymentRequest();
        req.setPaymentType(PaymentType.PURCHASE);
        req.setAmount(20.0);

        PaymentHandler next = mock(PaymentHandler.class);
        handler.setNext(next);

        handler.handle(req);

        verify(next, times(1)).handle(req);
        verifyNoInteractions(userRepo);
        verifyNoInteractions(requestRepo);
    }

    @Test
    void handle_withNoNext_andNonTopUp_doesNothing() {
        PaymentRequest req = new PaymentRequest();
        req.setPaymentType(PaymentType.PURCHASE);
        req.setAmount(20.0);

        assertDoesNotThrow(() -> handler.handle(req));
        verifyNoInteractions(userRepo);
        verifyNoInteractions(requestRepo);
    }
}
