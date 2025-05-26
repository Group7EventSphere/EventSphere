package id.ac.ui.cs.advprog.eventspherre.config;

import id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler;
import id.ac.ui.cs.advprog.eventspherre.handler.TopUpHandler;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentChainConfigTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private PaymentRequestRepository reqRepo;
    @Mock
    private ExecutorService executor;

    private PaymentChainConfig config;
    private TopUpHandler topUpHandler;
    private PaymentHandler chainHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new PaymentChainConfig(userRepo, reqRepo);
        topUpHandler = config.topUpHandler();
        chainHandler = config.paymentHandlerChain(executor, topUpHandler);
    }

    @Test
    void testPaymentExecutorBean() {
        ExecutorService pool = config.paymentExecutor();
        assertNotNull(pool, "ExecutorService bean should not be null");
        assertTrue(pool instanceof ThreadPoolExecutor,
                   "ExecutorService should be a ThreadPoolExecutor");
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) pool;
        assertEquals(10, tpe.getCorePoolSize(),
                     "ThreadPoolExecutor should have a core pool size of 10");
    }

    @Test
    void testTopUpHandlerProcessesRequest() {
        // Arrange
        User user = new User();
        user.setId(42);
        user.setBalance(100.0);

        PaymentRequest req = mock(PaymentRequest.class);
        when(req.getPaymentType()).thenReturn(PaymentRequest.PaymentType.TOPUP);
        when(req.getAmount()).thenReturn(50.0);
        when(req.getUser()).thenReturn(user);
        when(userRepo.findById(42)).thenReturn(Optional.of(user));

        // Act
        topUpHandler.handle(req);

        // Assert balance updated and flags set
        assertEquals(150.0, user.getBalance(), 1e-6, "Balance should increase by amount");
        verify(req).setProcessed(true);
        // Optionally verify repository save calls if implemented
        verify(userRepo).findById(42);
        verify(reqRepo).save(req);
    }

    @Test
    void testPaymentHandlerChainSubmitsToExecutor() {
        // Arrange
        PaymentRequest req = mock(PaymentRequest.class);
        when(req.getPaymentType()).thenReturn(PaymentRequest.PaymentType.TOPUP);
        when(req.getAmount()).thenReturn(20.0);

        // Act
        chainHandler.handle(req);

        // Assert that executor.submit() was called exactly once
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).submit(captor.capture());
        assertNotNull(captor.getValue(), "Runnable submitted to executor should not be null");
    }
    
    @Test
    void testPaymentHandlerChainSetNext() {
        PaymentHandler mockHandler = mock(PaymentHandler.class);
        
        chainHandler.setNext(mockHandler);
        

        assertNotNull(chainHandler, "Chain handler should not be null after setNext");
        
        PaymentRequest req = mock(PaymentRequest.class);
        when(req.getPaymentType()).thenReturn(PaymentRequest.PaymentType.TOPUP);
        
        chainHandler.handle(req);
        
        verify(executor, atLeastOnce()).submit(any(Runnable.class));
    }

    @Test
    void testPaymentHandlerChainHandleExecutesAsynchronously() {
        PaymentRequest req = mock(PaymentRequest.class);
        when(req.getPaymentType()).thenReturn(PaymentRequest.PaymentType.TOPUP);
        when(req.getAmount()).thenReturn(25.0);

        chainHandler.handle(req);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).submit(runnableCaptor.capture());
        
        Runnable submittedTask = runnableCaptor.getValue();
        assertNotNull(submittedTask, "Submitted task should not be null");
        
        verify(executor).submit(any(Runnable.class));
    }

    @Test
    void testLambdaHandleExecutesSufficientHandler() {
        User user = new User();
        user.setId(1);
        user.setBalance(100.0);

        PaymentRequest req = mock(PaymentRequest.class);
        when(req.getPaymentType()).thenReturn(PaymentRequest.PaymentType.PURCHASE);
        when(req.getAmount()).thenReturn(50.0);
        when(req.getUser()).thenReturn(user);
        when(userRepo.findById(1)).thenReturn(Optional.of(user));

        chainHandler.handle(req);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).submit(runnableCaptor.capture());
        
        Runnable lambdaTask = runnableCaptor.getValue();
        assertNotNull(lambdaTask, "Lambda task should not be null");

        lambdaTask.run();


        verify(req, atLeastOnce()).getPaymentType();
        verify(req, atLeastOnce()).getAmount();
        verify(req, atLeastOnce()).getUser();
    }
}
