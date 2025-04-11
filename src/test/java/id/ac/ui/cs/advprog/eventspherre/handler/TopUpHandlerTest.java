package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;

class TopUpHandlerTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("TopUp User");
        user.setEmail("topup@example.com");
        user.setPassword("password");
        user.setRole(User.Role.ATTENDEE);
        user.setBalance(100.0);
    }

    @Test
    void testTopUpHandlerProcessesTopUpRequest() {
        double topUpAmount = 50.0;
        PaymentRequest request = new PaymentRequest(user, topUpAmount, PaymentRequest.PaymentType.TOPUP);

        TopUpHandler topUpHandler = new TopUpHandler();
        DummyHandler dummy = new DummyHandler();
        topUpHandler.setNext(dummy);

        topUpHandler.handle(request);


        assertTrue(request.isProcessed());
        assertEquals("Top-up successful: balance added", request.getMessage());
        assertEquals(150.0, user.getBalance(), 0.001);

        assertFalse(dummy.wasCalled());
    }

    // Dummy handler for verifying that the chain does not proceed.
    private static class DummyHandler implements PaymentHandler {
        private boolean called = false;

        @Override
        public void setNext(PaymentHandler handler) {}

        @Override
        public void handle(PaymentRequest request) {
            called = true;
        }

        boolean wasCalled() {
            return called;
        }
    }

    @Test
    void testUserBalanceIncreasesAfterTopUp() {
        double initialBalance = user.getBalance();
        double topUpAmount = 75.0;
        PaymentRequest request = new PaymentRequest(user, topUpAmount, PaymentRequest.PaymentType.TOPUP);

        TopUpHandler topUpHandler = new TopUpHandler();
        // Using a dummy handler to check that the chain stops.
        DummyHandler dummy = new DummyHandler();
        topUpHandler.setNext(dummy);

        // Process top-up.
        topUpHandler.handle(request);

        double expectedBalance = initialBalance + topUpAmount;
        assertEquals(expectedBalance, user.getBalance(), 0.001);
        assertTrue(user.getBalance() > initialBalance);
    }
}
