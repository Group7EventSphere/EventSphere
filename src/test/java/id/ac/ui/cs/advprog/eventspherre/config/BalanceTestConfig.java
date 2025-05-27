package id.ac.ui.cs.advprog.eventspherre.config;

import id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class BalanceTestConfig {

    @Bean
    public ExecutorService mockExecutorService() {
        ExecutorService mockExec = mock(ExecutorService.class);
        try {
            given(mockExec.awaitTermination(anyLong(), any(TimeUnit.class))).willReturn(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return mockExec;
    }

    @Bean
    public PaymentHandler mockPaymentHandler() {
        return new PaymentHandler() {
            @Override 
            public void setNext(PaymentHandler next) {
                // No next handler in this test configuration
            }
            
            @Override 
            public void handle(PaymentRequest req) {
                User u = req.getUser();
                u.setBalance(u.getBalance() + req.getAmount());
                req.setMessage("Top-up successful: balance added");
            }
        };
    }
}