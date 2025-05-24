package id.ac.ui.cs.advprog.eventspherre.config;

import id.ac.ui.cs.advprog.eventspherre.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enable method-level security such as @PreAuthorize
@Profile("!test") // Do not apply during test profile
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(
        AuthenticationProvider authenticationProvider,
        JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/api/**")
                )
            )
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Configure route security
            .authorizeHttpRequests(auth -> auth
                // Public web routes
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/fonts/**", "/error", "/unauthorized").permitAll()
                .requestMatchers("/", "/api/auth/**", "/api/rabbitmq/**", "/api/events/**").permitAll() // Allow access to dashboard without authentication
                .requestMatchers("/admin/**").hasRole("ADMIN") // Require ADMIN role for admin pages
                .requestMatchers("/events/manage/**", "/api/events/**").hasAnyRole("ADMIN", "ORGANIZER")
                .requestMatchers("/balance/**").hasAnyRole("ATTENDEE", "ORGANIZER") // Require ATTENDEE or ORGANIZER role for balance pages
                .requestMatchers("/reviews/**").hasAnyRole("ADMIN", "ORGANIZER", "ATTENDEE")// Require ADMIN or ORGANIZER role for managing events
                .anyRequest().authenticated()
            )
            // Configure form login (traditional web flow)
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // Configure logout
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // Configure session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            // Configure authentication provider
            .authenticationProvider(authenticationProvider)
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Configure exception handling
            .exceptionHandling(exceptionHandling -> exceptionHandling
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
                // Redirect to a custom unauthorized page (for example, "/unauthorized")
                response.sendRedirect("/unauthorized");
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Allow all origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow all common HTTP methods
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Allow common headers
        configuration.setExposedHeaders(List.of("Authorization")); // Expose Authorization header
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // Only apply to API endpoints
        return source;
    }
}