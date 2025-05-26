package id.ac.ui.cs.advprog.eventspherre.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This is a simplified security configuration for test cases
 * that don't explicitly import WebSecurityTestConfig
 */
@Configuration
@EnableWebSecurity
@Profile("test") // Only active during tests
@Order(90) // Lower priority than WebSecurityTestConfig
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain simpleTestSecurityFilterChain(HttpSecurity http) throws Exception {
        // Simple security configuration for tests that permits all requests
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}