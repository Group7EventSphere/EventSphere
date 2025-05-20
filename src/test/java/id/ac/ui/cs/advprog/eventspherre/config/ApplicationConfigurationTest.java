package id.ac.ui.cs.advprog.eventspherre.config;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationConfigurationTest {

    private UserRepository userRepository;
    private ApplicationConfiguration config;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        config = new ApplicationConfiguration(userRepository);
    }

    @Test
    void userDetailsService_loads_existing_user() {
        // arrange: make repo return a domain User
        User domainUser = new User();
        domainUser.setId(42);
        domainUser.setEmail("alice@example.com");
        domainUser.setPassword("doesntMatter");
        when(userRepository.findByEmail("alice@example.com"))
            .thenReturn(Optional.of(domainUser));

        // act
        UserDetailsService uds = config.userDetailsService();
        var loaded = uds.loadUserByUsername("alice@example.com");

        // assert
        // (we expect the same object, since our User implements UserDetails)
        assertThat(loaded).isSameAs(domainUser);
    }

    @Test
    void userDetailsService_throws_when_user_not_found() {
        when(userRepository.findByEmail("bob@nowhere")).thenReturn(Optional.empty());
        UserDetailsService uds = config.userDetailsService();

        assertThatThrownBy(() -> uds.loadUserByUsername("bob@nowhere"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    void passwordEncoder_is_bcrypt_and_matches() {
        BCryptPasswordEncoder encoder = config.passwordEncoder();
        String raw = "secret";
        String hashed = encoder.encode(raw);

        assertThat(encoder.matches(raw, hashed)).isTrue();
    }

    @Test
    void authenticationProvider_authenticates_with_correct_password() {
        // arrange: create a user whose password is BCrypt("pw")
        BCryptPasswordEncoder encoder = config.passwordEncoder();
        String raw = "pw";
        String encoded = encoder.encode(raw);

        User domainUser = new User();
        domainUser.setEmail("u@example.com");
        domainUser.setPassword(encoded);
        // no roles/authorities needed for basic auth
        when(userRepository.findByEmail("u@example.com"))
            .thenReturn(Optional.of(domainUser));

        AuthenticationProvider provider = config.authenticationProvider();

        // act: attempt authentication
        UsernamePasswordAuthenticationToken authReq =
            new UsernamePasswordAuthenticationToken("u@example.com", raw);
        Authentication auth = provider.authenticate(authReq);

        // assert
        assertThat(auth.isAuthenticated()).isTrue();
        // principal should be our domain User object
        assertThat(auth.getPrincipal()).isSameAs(domainUser);
    }
}
