package id.ac.ui.cs.advprog.eventspherre.config;

import id.ac.ui.cs.advprog.eventspherre.handler.DeductBalanceHandler;
import id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler;
import id.ac.ui.cs.advprog.eventspherre.handler.SufficientBalanceHandler;
import id.ac.ui.cs.advprog.eventspherre.handler.TopUpHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentChainConfig {

    @Bean
    public PaymentHandler paymentHandlerChain() {
        SufficientBalanceHandler sufficientBalance = new SufficientBalanceHandler();
        DeductBalanceHandler deductBalance = new DeductBalanceHandler();
        TopUpHandler topUp = new TopUpHandler();

        sufficientBalance.setNext(deductBalance);
        deductBalance.setNext(topUp);

        return sufficientBalance;
    }
}
