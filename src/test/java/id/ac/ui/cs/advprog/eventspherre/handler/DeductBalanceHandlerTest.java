package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeductBalanceHandlerTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Deduct User");
        user.setEmail("deduct@example.com");
        user.setPassword("password");
        user.setRole(User.Role.ATTENDEE);
    }

    @Test
    void testDeductBalanceHandlerProcessesPurchase() {
        // User has sufficient balance for purchase.
        user.setBalance(200.0);
        PaymentRequest request = new PaymentRequest(user, 150.0, PaymentRequest.PaymentType.PURCHASE);

        DeductBalanceHandler handler = new DeductBalanceHandler();
        // No next handler for this test (end of chain).
        handler.handle(request);

        // Expect that after deduction, the request is marked processed,
        assertTrue(request.isProcessed());
        assertEquals("Purchase successful: balance deducted", request.getMessage());
        assertEquals(50.0, user.getBalance(), 0.001);
    }
}
