package id.ac.ui.cs.advprog.eventspherre.model;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentProcessingTest {
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setName("Test Attendee");
        user.setEmail("attendee@example.com");
        user.setPassword("password");
        user.setRole(User.Role.ATTENDEE);
        user.setBalance(100.0);
    }

    @Test
    void testPaymentRequestCreation() {
        double amount = 50.0;
        PaymentRequest request = new PaymentRequest(user, amount, PaymentRequest.PaymentType.TOPUP);

        assertNotNull(request);
        assertEquals(user, request.getUser());
        assertEquals(amount, request.getAmount(), 0.001);
        assertEquals(PaymentRequest.PaymentType.TOPUP, request.getPaymentType());
        assertFalse(request.isProcessed());
        assertNull(request.getMessage());
    }

    @Test
    void testPurchasePaymentRequestCreation() {
        User user = new User();
        user.setName("Test Buyer");
        user.setEmail("buyer@example.com");
        user.setPassword("password");
        user.setRole(User.Role.ATTENDEE);

        double amount = 150.0;
        PaymentRequest request = new PaymentRequest(user, amount, PaymentRequest.PaymentType.PURCHASE);

        assertNotNull(request);
        assertEquals(user, request.getUser());
        assertEquals(amount, request.getAmount(), 0.001);
        assertEquals(PaymentRequest.PaymentType.PURCHASE, request.getPaymentType());
        assertFalse(request.isProcessed());
        assertNull(request.getMessage());
    }

    @Test
    void testMarkRequestAsFailed() {
        User user = new User();
        user.setName("Test Buyer");
        user.setEmail("buyer@example.com");
        user.setPassword("password");
        user.setRole(User.Role.ATTENDEE);

        double purchaseAmount = 150.0;
        PaymentRequest request = new PaymentRequest(user, purchaseAmount, PaymentRequest.PaymentType.PURCHASE);

        request.setProcessed(true);
        request.setMessage("Payment failed: insufficient funds");

        assertTrue(request.isProcessed());
        assertEquals("Payment failed: insufficient funds", request.getMessage());
    }


    @Test
    void testPaymentRequestToString() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setRole(User.Role.ATTENDEE);
        PaymentRequest request = new PaymentRequest(user, 100.0, PaymentRequest.PaymentType.TOPUP);
        String str = request.toString();
        assertNotNull(str);
        assertFalse(str.isEmpty());
    }
}
