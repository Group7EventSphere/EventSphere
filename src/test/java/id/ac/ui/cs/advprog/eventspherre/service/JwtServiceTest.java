package id.ac.ui.cs.advprog.eventspherre.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private String secretKey;
    private long expirationMillis = 1_000; // 1 second for easy expiration testing

    @BeforeEach
    void setUp() {
        // 1) Generate a random HMAC-SHA256 key and Base64-encode it:
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        secretKey = Base64.getEncoder().encodeToString(key.getEncoded());

        // 2) Instantiate service and inject fields via ReflectionTestUtils
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expirationMillis);

        // 3) A simple UserDetails with username "john"
        userDetails = User
            .withUsername("john")
            .password("doesntMatter")
            .authorities(Collections.emptyList())
            .build();
    }

    @Test
    void generateToken_and_extractUsername_and_validate() {
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotBlank();

        // subject should be the username
        String extracted = jwtService.extractUsername(token);
        assertThat(extracted).isEqualTo("john");

        // valid immediately after creation
        boolean valid = jwtService.isTokenValid(token, userDetails);
        assertThat(valid).isTrue();
    }

    @Test
    void token_expires_when_past_expiration() throws InterruptedException {
        String token = jwtService.generateToken(userDetails);
    
        // make sure it’s valid immediately
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    
        // wait past the configured 1 sec TTL
        Thread.sleep(expirationMillis + 50);
    
        // now assert exception
        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
            .isInstanceOf(ExpiredJwtException.class)
            .hasMessageContaining("JWT expired");
    }

    @Test
    void customClaims_are_available_via_extractClaim() {
        Map<String, Object> extra = new HashMap<>();
        extra.put("role", "ADMIN");

        String token = jwtService.generateToken(extra, userDetails);

        // pull out our custom "role" claim
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void getExpirationTime_returns_configured_value() {
        assertThat(jwtService.getExpirationTime()).isEqualTo(expirationMillis);
    }
}
