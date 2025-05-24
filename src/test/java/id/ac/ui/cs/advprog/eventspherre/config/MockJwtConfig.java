package id.ac.ui.cs.advprog.eventspherre.config;

import id.ac.ui.cs.advprog.eventspherre.service.JwtService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Provides mock implementations of JWT-related beans for the test environment
 * to prevent conflicts with the actual implementations.
 */
@Configuration
@Profile("test")
public class MockJwtConfig {

    @Bean
    @Primary
    public JwtService mockJwtService() {
        return Mockito.mock(JwtService.class);
    }
    
    @Bean(name = "jwtHandlerExceptionResolver")
    @Primary
    public HandlerExceptionResolver mockJwtHandlerExceptionResolver() {
        return Mockito.mock(HandlerExceptionResolver.class);
    }
}