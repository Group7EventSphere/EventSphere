package id.ac.ui.cs.advprog.eventspherre.filter;

import id.ac.ui.cs.advprog.eventspherre.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HandlerExceptionResolver jwtHandlerExceptionResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(
                jwtService,
                userDetailsService,
                jwtHandlerExceptionResolver
        );

        userDetails = new User(
                "test@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void doFilterInternal_WithNonApiUrl_ShouldSkipJwtFilter() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/events");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_WithPublicApiEndpoint_ShouldSkipJwtFilter() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_WithApiSignupEndpoint_ShouldSkipJwtFilter() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/signup");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_WithApiRefreshTokenEndpoint_ShouldSkipJwtFilter() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/refresh-token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_WithoutAuthorizationHeader_ShouldSkipAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_WithInvalidAuthorizationHeader_ShouldSkipAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Basic dGVzdA==");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_WithValidJwtToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        String jwt = "valid.jwt.token";
        String authHeader = "Bearer " + jwt;
        String userEmail = "test@example.com";

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, userDetails)).thenReturn(true);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_WithValidJwtButExistingAuthentication_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String jwt = "valid.jwt.token";
        String authHeader = "Bearer " + jwt;
        String userEmail = "test@example.com";

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);

        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(existingAuth);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(userDetailsService);
        }
    }

    @Test
    void doFilterInternal_WithInvalidJwtToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String jwt = "invalid.jwt.token";
        String authHeader = "Bearer " + jwt;
        String userEmail = "test@example.com";

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, userDetails)).thenReturn(false);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_WithExpiredJwtException_ShouldHandleException() throws ServletException, IOException {
        // Arrange
        String jwt = "expired.jwt.token";
        String authHeader = "Bearer " + jwt;

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtHandlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(ExpiredJwtException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithMalformedJwtException_ShouldHandleException() throws ServletException, IOException {
        // Arrange
        String jwt = "malformed.jwt.token";
        String authHeader = "Bearer " + jwt;

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenThrow(new MalformedJwtException("Malformed token"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtHandlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(MalformedJwtException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithUnsupportedJwtException_ShouldHandleException() throws ServletException, IOException {
        // Arrange
        String jwt = "unsupported.jwt.token";
        String authHeader = "Bearer " + jwt;

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenThrow(new UnsupportedJwtException("Unsupported token"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtHandlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(UnsupportedJwtException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithSignatureException_ShouldHandleException() throws ServletException, IOException {
        // Arrange
        String jwt = "invalid.signature.token";
        String authHeader = "Bearer " + jwt;

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenThrow(new SignatureException("Invalid signature"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtHandlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(SignatureException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithIllegalArgumentException_ShouldHandleException() throws ServletException, IOException {
        // Arrange
        String jwt = "illegal.jwt.token";
        String authHeader = "Bearer " + jwt;

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenThrow(new IllegalArgumentException("Illegal argument"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtHandlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(IllegalArgumentException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNullUserEmail_ShouldSkipAuthentication() throws ServletException, IOException {
        // Arrange
        String jwt = "valid.jwt.token";
        String authHeader = "Bearer " + jwt;

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(null);        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(userDetailsService);
        }
    }
}
