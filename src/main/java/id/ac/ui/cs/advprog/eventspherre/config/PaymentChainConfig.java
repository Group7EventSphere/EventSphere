package id.ac.ui.cs.advprog.eventspherre.config;

import id.ac.ui.cs.advprog.eventspherre.handler.DeductBalanceHandler;
import id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler;
import id.ac.ui.cs.advprog.eventspherre.handler.SufficientBalanceHandler;
import id.ac.ui.cs.advprog.eventspherre.handler.TopUpHandler;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class PaymentChainConfig {

    @Bean
    public ExecutorService paymentExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean
    public PaymentHandler paymentHandlerChain(ExecutorService exec) {
        SufficientBalanceHandler sufficient = new SufficientBalanceHandler();
        DeductBalanceHandler deduct         = new DeductBalanceHandler();
        TopUpHandler topUp                  = new TopUpHandler();

        sufficient.setNext(deduct);
        deduct.setNext(topUp);

        return new PaymentHandler() {
            @Override
            public void setNext(PaymentHandler handler) {
                sufficient.setNext(handler);
            }

            @Override
            public void handle(PaymentRequest request) {
                exec.submit(() -> sufficient.handle(request));
            }
        };
    }
}