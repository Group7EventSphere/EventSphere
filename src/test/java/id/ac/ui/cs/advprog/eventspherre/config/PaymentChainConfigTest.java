package id.ac.ui.cs.advprog.eventspherre.config;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest.PaymentType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentChainConfigTest {

    private PaymentChainConfig config;

    @BeforeEach
    void setUp() {
        config = new PaymentChainConfig();
    }

    @Test
    void processPurchase_withSufficientBalance() {
        User user = new User();
        user.setBalance(100.0);
        PaymentRequest request = new PaymentRequest(user, 60.0, PaymentType.PURCHASE);

        config.paymentHandlerChain().handle(request);

        // Only check final balance
        assertEquals(40.0, user.getBalance(), 1e-9);
    }

    @Test
    void processPurchase_withInsufficientBalance() {
        User user = new User();
        user.setBalance(30.0);
        PaymentRequest request = new PaymentRequest(user, 50.0, PaymentType.PURCHASE);

        config.paymentHandlerChain().handle(request);

        // Balance remains unchanged on failure
        assertEquals(30.0, user.getBalance(), 1e-9);
    }

    @Test
    void processTopUp_increasesBalance() {
        User user = new User();
        user.setBalance(20.0);
        PaymentRequest request = new PaymentRequest(user, 50.0, PaymentType.TOPUP);

        config.paymentHandlerChain().handle(request);

        // Only check final balance
        assertEquals(70.0, user.getBalance(), 1e-9);
    }

    @Test
    void processSequentialTopUpThenPurchase() {
        User user = new User();
        user.setBalance(0.0);

        // Top-up first
        PaymentRequest topUp = new PaymentRequest(user, 100.0, PaymentType.TOPUP);
        config.paymentHandlerChain().handle(topUp);
        assertEquals(100.0, user.getBalance(), 1e-9);

        // Then purchase
        PaymentRequest purchase = new PaymentRequest(user, 80.0, PaymentType.PURCHASE);
        config.paymentHandlerChain().handle(purchase);
        assertEquals(20.0, user.getBalance(), 1e-9);
    }

    @Test
    void processPurchaseFailure_thenTopUp_thenPurchaseSuccess() {
        User user = new User();
        user.setBalance(40.0);

        // First purchase fails
        PaymentRequest failPurchase = new PaymentRequest(user, 60.0, PaymentType.PURCHASE);
        config.paymentHandlerChain().handle(failPurchase);
        assertEquals(40.0, user.getBalance(), 1e-9);

        // Top-up
        PaymentRequest topUp = new PaymentRequest(user, 30.0, PaymentType.TOPUP);
        config.paymentHandlerChain().handle(topUp);
        assertEquals(70.0, user.getBalance(), 1e-9);

        // Second purchase succeeds
        PaymentRequest secondPurchase = new PaymentRequest(user, 60.0, PaymentType.PURCHASE);
        config.paymentHandlerChain().handle(secondPurchase);
        assertEquals(10.0, user.getBalance(), 1e-9);
    }

    @Test
    void processZeroOrNegativeAmounts_areIgnored() {
        User user = new User();
        user.setBalance(50.0);

        // Zero purchase
        PaymentRequest zero = new PaymentRequest(user, 0.0, PaymentType.PURCHASE);
        config.paymentHandlerChain().handle(zero);
        assertEquals(50.0, user.getBalance(), 1e-9);

        // Negative top-up
        PaymentRequest negativeTopUp = new PaymentRequest(user, -10.0, PaymentType.TOPUP);
        config.paymentHandlerChain().handle(negativeTopUp);
        assertEquals(50.0, user.getBalance(), 1e-9);
    }
}