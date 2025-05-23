package id.ac.ui.cs.advprog.eventspherre.config;

import id.ac.ui.cs.advprog.eventspherre.handler.*;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.springframework.context.annotation.Primary;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
public class PaymentChainConfig {

    private final UserRepository           userRepo;
    private final PaymentRequestRepository requestRepo;

    @Bean
    public ExecutorService paymentExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean
    public TopUpHandler topUpHandler() {
        return new TopUpHandler(userRepo, requestRepo);
    }

    @Bean
    @Primary

    public PaymentHandler paymentHandlerChain(ExecutorService executor,
                                              TopUpHandler    topUp) {
        SufficientBalanceHandler sufficient = new SufficientBalanceHandler();
        DeductBalanceHandler     deduct     = new DeductBalanceHandler();

        sufficient.setNext(deduct);
        deduct.setNext(topUp);

        return new PaymentHandler() {
            @Override public void setNext(PaymentHandler h) { sufficient.setNext(h); }
            @Override public void handle(id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest r) {
                executor.submit(() -> sufficient.handle(r));
            }
        };
    }
}