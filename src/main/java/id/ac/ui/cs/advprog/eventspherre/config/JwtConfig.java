package id.ac.ui.cs.advprog.eventspherre.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration
@Profile("!test")
public class JwtConfig {
    
    @Value("${jwt.secret:#{T(java.util.UUID).randomUUID().toString()}}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private Long expiration;
    
    @Value("${jwt.refresh.expiration:604800000}") // 7 days in milliseconds
    private Long refreshExpiration;
    
    @Bean
    public SecretKey secretKey() {
        if (this.secret == null || this.secret.length() < 32) {
            // Generate a secure random key if the secret is not provided or too short
            byte[] keyBytes = new byte[32];
            new SecureRandom().nextBytes(keyBytes);
            this.secret = Base64.getEncoder().encodeToString(keyBytes);
        }
        
        byte[] keyBytes = this.secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
    
    public Long getExpiration() {
        return expiration;
    }
    
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}