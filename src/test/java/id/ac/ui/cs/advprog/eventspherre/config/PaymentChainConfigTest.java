package id.ac.ui.cs.advprog.eventspherre.config;

import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest.PaymentType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PaymentChainConfigTest {

    private PaymentChainConfig config;

    @BeforeEach
    void setUp() {
        config = new PaymentChainConfig();
    }

    private void handleAndAwait(ExecutorService exec, 
                                id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler chain, 
                                PaymentRequest req) throws InterruptedException {
        chain.handle(req);
        exec.shutdown();
        assertTrue(exec.awaitTermination(1, TimeUnit.SECONDS),
                   "Executor did not terminate in time");
    }

    public static User makeDummyUser() {
        User u = new User();        u.setId(1);
        u.setName("Test User");
        u.setEmail("test@example.com");
        u.setPassword("password");
        u.setRole(User.Role.ATTENDEE);
        u.setBalance(100.0);
        return u;
    }
        
    
    @Test
    void processPurchase_withSufficientBalance() throws InterruptedException {
        User user = makeDummyUser();
        user.setBalance(100.0);
        PaymentRequest request = new PaymentRequest(user, 60.0, PaymentType.PURCHASE);

        ExecutorService exec = config.paymentExecutor();
        var chain = config.paymentHandlerChain(exec);
        handleAndAwait(exec, chain, request);

        assertEquals(40.0, user.getBalance(), 1e-9);
    }

    @Test
    void processPurchase_withInsufficientBalance() throws InterruptedException {
        User user = makeDummyUser();
        user.setBalance(30.0);
        PaymentRequest request = new PaymentRequest(user, 50.0, PaymentType.PURCHASE);

        ExecutorService exec = config.paymentExecutor();
        var chain = config.paymentHandlerChain(exec);
        handleAndAwait(exec, chain, request);

        assertEquals(30.0, user.getBalance(), 1e-9);
    }

    @Test
    void processTopUp_increasesBalance() throws InterruptedException {
        User user = makeDummyUser();
        user.setBalance(20.0);
        PaymentRequest request = new PaymentRequest(user, 50.0, PaymentType.TOPUP);

        ExecutorService exec = config.paymentExecutor();
        var chain = config.paymentHandlerChain(exec);
        handleAndAwait(exec, chain, request);

        assertEquals(70.0, user.getBalance(), 1e-9);
    }

    @Test
    void processSequentialTopUpThenPurchase() throws InterruptedException {
        User user = makeDummyUser();
        user.setBalance(0.0);

        // Step 1: top-up
        {
            ExecutorService exec1 = config.paymentExecutor();
            var chain1 = config.paymentHandlerChain(exec1);
            PaymentRequest topUp = new PaymentRequest(user, 100.0, PaymentType.TOPUP);
            handleAndAwait(exec1, chain1, topUp);
            assertEquals(100.0, user.getBalance(), 1e-9);
        }
        // Step 2: purchase
        {
            ExecutorService exec2 = config.paymentExecutor();
            var chain2 = config.paymentHandlerChain(exec2);
            PaymentRequest purchase = new PaymentRequest(user, 80.0, PaymentType.PURCHASE);
            handleAndAwait(exec2, chain2, purchase);
            assertEquals(20.0, user.getBalance(), 1e-9);
        }
    }

    @Test
    void processPurchaseFailure_thenTopUp_thenPurchaseSuccess() throws InterruptedException {
        User user = makeDummyUser();
        user.setBalance(40.0);

        // 1) failing purchase
        {
            ExecutorService exec1 = config.paymentExecutor();
            var chain1 = config.paymentHandlerChain(exec1);
            PaymentRequest fail = new PaymentRequest(user, 60.0, PaymentType.PURCHASE);
            handleAndAwait(exec1, chain1, fail);
            assertEquals(40.0, user.getBalance(), 1e-9);
        }
        // 2) top-up
        {
            ExecutorService exec2 = config.paymentExecutor();
            var chain2 = config.paymentHandlerChain(exec2);
            PaymentRequest topUp = new PaymentRequest(user, 30.0, PaymentType.TOPUP);
            handleAndAwait(exec2, chain2, topUp);
            assertEquals(70.0, user.getBalance(), 1e-9);
        }
        // 3) successful purchase
        {
            ExecutorService exec3 = config.paymentExecutor();
            var chain3 = config.paymentHandlerChain(exec3);
            PaymentRequest ok = new PaymentRequest(user, 60.0, PaymentType.PURCHASE);
            handleAndAwait(exec3, chain3, ok);
            assertEquals(10.0, user.getBalance(), 1e-9);
        }
    }

    @Test
    void processZeroOrNegativeAmounts_areIgnored() throws InterruptedException {
        User user = makeDummyUser();
        user.setBalance(50.0);

        // Zero purchase
        {
            ExecutorService exec1 = config.paymentExecutor();
            var chain1 = config.paymentHandlerChain(exec1);
            PaymentRequest zero = new PaymentRequest(user, 0.0, PaymentType.PURCHASE);
            handleAndAwait(exec1, chain1, zero);
            assertEquals(50.0, user.getBalance(), 1e-9);
        }
        // Negative top-up
        {
            ExecutorService exec2 = config.paymentExecutor();
            var chain2 = config.paymentHandlerChain(exec2);
            PaymentRequest neg = new PaymentRequest(user, -10.0, PaymentType.TOPUP);
            handleAndAwait(exec2, chain2, neg);
            assertEquals(50.0, user.getBalance(), 1e-9);
        }
    }

     @Test
    void concurrentTopUps() throws InterruptedException {
        User user = makeDummyUser();
        user.setBalance(0.0);

        int n = 50;               // number of concurrent requests
        double amount = 5.0;      // each request tops up by 5
        ExecutorService exec = config.paymentExecutor();
        var chain = config.paymentHandlerChain(exec);

        // fire off N requests
        for (int i = 0; i < n; i++) {
            chain.handle(new PaymentRequest(user, amount, PaymentType.TOPUP));
        }

        // no more submissions, wait for them to finish
        exec.shutdown();
        assertTrue(exec.awaitTermination(5, TimeUnit.SECONDS),
                   "Timed out waiting for top ups");

        // if everything ran thread‐safely, balance == n * amount
        assertEquals(n * amount, user.getBalance(), 1e-9);
    }

    /**
     * Submit N purchase requests in parallel and check final balance.
     */
    @Test
    void concurrentPurchases() throws InterruptedException {
        int n = 20;
        double purchase = 2.5;
        User user = makeDummyUser();        // seed with exactly enough funds
        user.setBalance(n * purchase);

        ExecutorService exec = config.paymentExecutor();
        var chain = config.paymentHandlerChain(exec);

        for (int i = 0; i < n; i++) {
            chain.handle(new PaymentRequest(user, purchase, PaymentType.PURCHASE));
        }

        exec.shutdown();
        assertTrue(exec.awaitTermination(5, TimeUnit.SECONDS),
                   "Timed out waiting for purchases");

        assertEquals(0.0, user.getBalance(), 1e-9);
    }

    /**
     * Mix top‐ups and purchases concurrently to make sure they interleave safely.
     */
    @Test
    void mixedConcurrentOperations() throws InterruptedException {
        User user = makeDummyUser();
        user.setBalance(100.0);    // start with some buffer

        ExecutorService exec = config.paymentExecutor();
        var chain = config.paymentHandlerChain(exec);

        // 30 top‐ups of +10 and 30 purchases of -5
        for (int i = 0; i < 30; i++) {
            chain.handle(new PaymentRequest(user, 10.0, PaymentType.TOPUP));
            chain.handle(new PaymentRequest(user, 5.0,  PaymentType.PURCHASE));
        }

        exec.shutdown();
        assertTrue(exec.awaitTermination(5, TimeUnit.SECONDS),
                   "Timed out waiting for mixed ops");

        // Net effect: + (30*10) – (30*5) + initial 100 = 100 +300 –150 =250
        assertEquals(250.0, user.getBalance(), 1e-9);
    }
}