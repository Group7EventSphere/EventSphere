package id.ac.ui.cs.advprog.eventspherre.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.util.List;

@Configuration
@Profile("!test")
public class JwtExceptionResolverConfig {
    
    @Bean(name = "jwtHandlerExceptionResolver")
    public HandlerExceptionResolver jwtHandlerExceptionResolver() {
        HandlerExceptionResolverComposite composite = new HandlerExceptionResolverComposite();
        composite.setOrder(100); // Lower priority to avoid conflicts
        composite.setExceptionResolvers(List.of(
                new ExceptionHandlerExceptionResolver(),
                new DefaultHandlerExceptionResolver()
        ));
        return composite;
    }
}