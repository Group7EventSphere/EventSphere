package id.ac.ui.cs.advprog.eventspherre.handler;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
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

    @Test
    void testDeductBalanceHandlerInsufficientFunds() {
        user.setBalance(100.0);
        PaymentRequest request = new PaymentRequest(user, 150.0, PaymentRequest.PaymentType.PURCHASE);

        DeductBalanceHandler handler = new DeductBalanceHandler();
        handler.handle(request);

        assertFalse(request.isProcessed());
        assertEquals("Payment failed: insufficient funds", request.getMessage());
        assertEquals(100.0, user.getBalance(), 0.001); // Balance should remain unchanged
    }


    @Test
    void testDeductBalanceHandlerAmountNotGreaterThanMinValidId() {
        // Test when amount is NOT greater than MIN_VALID_ID
        user.setBalance(200.0);
        double amountLessThanOrEqualToMinId = AppConstants.MIN_VALID_ID; // Amount equals MIN_VALID_ID
        PaymentRequest request = new PaymentRequest(user, amountLessThanOrEqualToMinId, PaymentRequest.PaymentType.PURCHASE);

        DeductBalanceHandler handler = new DeductBalanceHandler();
        handler.handle(request);

        // Should not process since amount is not greater than MIN_VALID_ID
        assertFalse(request.isProcessed());
        assertEquals(200.0, user.getBalance(), 0.001); // Balance should remain unchanged
    }

    @Test
    void testDeductBalanceHandlerAmountLessThanMinValidId() {
        // Test when amount is less than MIN_VALID_ID
        user.setBalance(200.0);
        double amountLessThanMinId = AppConstants.MIN_VALID_ID - 1; // Amount less than MIN_VALID_ID
        PaymentRequest request = new PaymentRequest(user, amountLessThanMinId, PaymentRequest.PaymentType.PURCHASE);

        DeductBalanceHandler handler = new DeductBalanceHandler();
        handler.handle(request);

        // Should not process since amount is less than MIN_VALID_ID
        assertFalse(request.isProcessed());
        assertEquals(200.0, user.getBalance(), 0.001); // Balance should remain unchanged
    }


}
