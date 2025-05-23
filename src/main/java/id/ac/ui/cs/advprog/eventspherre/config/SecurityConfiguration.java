package id.ac.ui.cs.advprog.eventspherre.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enable method-level security such as @PreAuthorize
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfiguration(
        AuthenticationProvider authenticationProvider
    ) {
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests()
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/fonts/**", "/error", "/unauthorized").permitAll()
                .requestMatchers("/").permitAll() // Allow access to dashboard without authentication
                .requestMatchers("/admin/**").hasRole("ADMIN") // Require ADMIN role for admin pages
                .requestMatchers("/events/manage/**").hasAnyRole("ADMIN", "ORGANIZER")
                .requestMatchers("/balance/**").hasAnyRole("ATTENDEE", "ORGANIZER") // Require ATTENDEE or ORGANIZER role for balance pages
                .requestMatchers("/reviews/**").hasAnyRole("ADMIN", "ORGANIZER", "ATTENDEE")// Require ADMIN or ORGANIZER role for managing events
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            .and()
            .logout()
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            .and()
            .authenticationProvider(authenticationProvider)
            .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler());

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // Redirect to a custom unauthorized page (for example, "/unauthorized")
            response.sendRedirect("/unauthorized");
        };
    }
}