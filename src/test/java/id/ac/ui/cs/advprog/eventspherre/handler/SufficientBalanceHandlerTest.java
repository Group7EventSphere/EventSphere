package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SufficientBalanceHandlerTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Purchase User");
        user.setEmail("purchase@example.com");
        user.setPassword("password");
        user.setRole(User.Role.ATTENDEE);
    }

    @Test
    void testInsufficientFunds() {
        // User has balance 100; request is for 150 (purchase).
        user.setBalance(100.0);
        PaymentRequest request = new PaymentRequest(user, 150.0, PaymentRequest.PaymentType.PURCHASE);

        SufficientBalanceHandler handler = new SufficientBalanceHandler();
        // Use a dummy handler to check that request is not forwarded.
        DummyHandler dummy = new DummyHandler();
        handler.setNext(dummy);

        handler.handle(request);

        assertTrue(request.isProcessed());
        assertEquals("Payment failed: insufficient funds", request.getMessage());
        // Dummy handler should not have been called.
        assertFalse(dummy.wasCalled());
    }

    @Test
    void testSufficientFundsPassThrough() {
        // User has balance 200; request is for 150 (purchase).
        user.setBalance(200.0);
        PaymentRequest request = new PaymentRequest(user, 150.0, PaymentRequest.PaymentType.PURCHASE);

        SufficientBalanceHandler handler = new SufficientBalanceHandler();
        DummyHandler dummy = new DummyHandler();
        handler.setNext(dummy);

        handler.handle(request);

        // In this scenario, the handler should not mark the request as processed.
        assertFalse(request.isProcessed());
        // It should pass the request to the next handler.
        assertTrue(dummy.wasCalled());
    }

    private static class DummyHandler implements PaymentHandler {
        private boolean called = false;
        @Override
        public void setNext(PaymentHandler handler) { }
        @Override
        public void handle(PaymentRequest request) {
            called = true;
        }
        boolean wasCalled() {
            return called;
        }
    }
}
