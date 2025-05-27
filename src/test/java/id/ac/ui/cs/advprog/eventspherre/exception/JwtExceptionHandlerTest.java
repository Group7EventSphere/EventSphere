package id.ac.ui.cs.advprog.eventspherre.exception;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtExceptionHandlerTest {

    private JwtExceptionHandler jwtExceptionHandler;

    @BeforeEach
    void setUp() {
        jwtExceptionHandler = new JwtExceptionHandler();
    }

    @Test
    void handleExpiredJwtException_ShouldReturnUnauthorizedResponse() {
        // Arrange
        ExpiredJwtException exception = new ExpiredJwtException(null, null, "JWT token has expired");

        // Act
        ResponseEntity<Map<String, String>> response = jwtExceptionHandler.handleExpiredJwtException(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AppConstants.JWT_ERROR_UNAUTHORIZED, response.getBody().get("error"));
        assertEquals(AppConstants.JWT_ERROR_EXPIRED, response.getBody().get("message"));
    }

    @Test
    void handleUnsupportedJwtException_ShouldReturnUnauthorizedResponse() {
        // Arrange
        UnsupportedJwtException exception = new UnsupportedJwtException("Unsupported JWT token");

        // Act
        ResponseEntity<Map<String, String>> response = jwtExceptionHandler.handleUnsupportedJwtException(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AppConstants.JWT_ERROR_UNAUTHORIZED, response.getBody().get("error"));
        assertEquals(AppConstants.JWT_ERROR_UNSUPPORTED, response.getBody().get("message"));
    }

    @Test
    void handleMalformedJwtException_ShouldReturnUnauthorizedResponse() {
        // Arrange
        MalformedJwtException exception = new MalformedJwtException("Malformed JWT token");

        // Act
        ResponseEntity<Map<String, String>> response = jwtExceptionHandler.handleMalformedJwtException(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AppConstants.JWT_ERROR_UNAUTHORIZED, response.getBody().get("error"));
        assertEquals(AppConstants.JWT_ERROR_MALFORMED, response.getBody().get("message"));
    }

    @Test
    void handleSignatureException_ShouldReturnUnauthorizedResponse() {
        // Arrange
        SignatureException exception = new SignatureException("Invalid JWT signature");

        // Act
        ResponseEntity<Map<String, String>> response = jwtExceptionHandler.handleSignatureException(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AppConstants.JWT_ERROR_UNAUTHORIZED, response.getBody().get("error"));
        assertEquals(AppConstants.JWT_ERROR_INVALID_SIGNATURE, response.getBody().get("message"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequestResponse() {
        // Arrange
        String errorMessage = "Invalid JWT token argument";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // Act
        ResponseEntity<Map<String, String>> response = jwtExceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AppConstants.JWT_ERROR_BAD_REQUEST, response.getBody().get("error"));
        assertEquals(errorMessage, response.getBody().get("message"));
    }

    @Test
    void handleIllegalArgumentException_WithNullMessage_ShouldReturnBadRequestResponse() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException();

        // Act
        ResponseEntity<Map<String, String>> response = jwtExceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AppConstants.JWT_ERROR_BAD_REQUEST, response.getBody().get("error"));
        // The message should be null or empty since the exception doesn't have a message
        assertTrue(response.getBody().get("message") == null || response.getBody().get("message").isEmpty());
    }

    @Test
    void allExceptionHandlers_ShouldReturnNonNullResponseBody() {
        // Test that all exception handlers return a proper response body
        ExpiredJwtException expiredException = new ExpiredJwtException(null, null, "Expired");
        UnsupportedJwtException unsupportedException = new UnsupportedJwtException("Unsupported");
        MalformedJwtException malformedException = new MalformedJwtException("Malformed");
        SignatureException signatureException = new SignatureException("Invalid signature");
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Illegal argument");

        ResponseEntity<Map<String, String>> expiredResponse = jwtExceptionHandler.handleExpiredJwtException(expiredException);
        ResponseEntity<Map<String, String>> unsupportedResponse = jwtExceptionHandler.handleUnsupportedJwtException(unsupportedException);
        ResponseEntity<Map<String, String>> malformedResponse = jwtExceptionHandler.handleMalformedJwtException(malformedException);
        ResponseEntity<Map<String, String>> signatureResponse = jwtExceptionHandler.handleSignatureException(signatureException);
        ResponseEntity<Map<String, String>> illegalArgumentResponse = jwtExceptionHandler.handleIllegalArgumentException(illegalArgumentException);

        assertNotNull(expiredResponse.getBody());
        assertNotNull(unsupportedResponse.getBody());
        assertNotNull(malformedResponse.getBody());
        assertNotNull(signatureResponse.getBody());
        assertNotNull(illegalArgumentResponse.getBody());

        // All responses should have both error and message fields
        assertNotNull(expiredResponse.getBody().get("error"));
        assertNotNull(expiredResponse.getBody().get("message"));
        assertNotNull(unsupportedResponse.getBody().get("error"));
        assertNotNull(unsupportedResponse.getBody().get("message"));
        assertNotNull(malformedResponse.getBody().get("error"));
        assertNotNull(malformedResponse.getBody().get("message"));
        assertNotNull(signatureResponse.getBody().get("error"));
        assertNotNull(signatureResponse.getBody().get("message"));
        assertNotNull(illegalArgumentResponse.getBody().get("error"));
        assertNotNull(illegalArgumentResponse.getBody().get("message"));
    }
}
