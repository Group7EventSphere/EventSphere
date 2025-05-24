package id.ac.ui.cs.advprog.eventspherre.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Special security configuration for tests that maintains the expected security behavior
 * for controller tests while avoiding conflicts with the JWT configuration.
 */
@TestConfiguration
@EnableWebSecurity
@EnableWebMvc
@Profile("test")
public class WebSecurityTestConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/api/**"))
            )
            .authorizeHttpRequests(auth -> auth
                // Public web routes
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/fonts/**", "/error", "/unauthorized").permitAll()
                .requestMatchers("/").permitAll()
                // Public API routes
                .requestMatchers("/api/auth/**").permitAll()
                // API endpoints that require authorization
                .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                .requestMatchers("/api/events/**").hasAnyRole("ADMIN", "ORGANIZER")
                // Admin only web routes
                // Admin-only routes - ensure these are properly protected
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/admin/transactions").hasRole("ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // Organizer web routes
                .requestMatchers("/events/manage/**").hasAnyRole("ADMIN", "ORGANIZER")
                // Allow balance-related URLs for any authenticated user including CSRF tokens
                .requestMatchers("/balance", "/balance/**").permitAll()
                // All other routes require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // Configure exception handling
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/unauthorized")
                .accessDeniedHandler(accessDeniedHandler())
            );
            
        return http.build();
    }
    
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // Check if it's an API request
            if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"You do not have permission to access this resource\"}");
            } else {
                // Redirect to a custom unauthorized page
                response.sendRedirect("/unauthorized");
            }
        };
    }
}