package id.ac.ui.cs.advprog.eventspherre.exception;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Profile("!test")
public class JwtExceptionHandler extends ResponseEntityExceptionHandler {    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, String>> handleExpiredJwtException(ExpiredJwtException e) {
        Map<String, String> response = new HashMap<>();
        response.put(AppConstants.RESPONSE_ERROR_KEY, AppConstants.JWT_ERROR_UNAUTHORIZED);
        response.put(AppConstants.RESPONSE_MESSAGE_KEY, AppConstants.JWT_ERROR_EXPIRED);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedJwtException(UnsupportedJwtException e) {
        Map<String, String> response = new HashMap<>();
        response.put(AppConstants.RESPONSE_ERROR_KEY, AppConstants.JWT_ERROR_UNAUTHORIZED);
        response.put(AppConstants.RESPONSE_MESSAGE_KEY, AppConstants.JWT_ERROR_UNSUPPORTED);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Map<String, String>> handleMalformedJwtException(MalformedJwtException e) {
        Map<String, String> response = new HashMap<>();
        response.put(AppConstants.RESPONSE_ERROR_KEY, AppConstants.JWT_ERROR_UNAUTHORIZED);
        response.put(AppConstants.RESPONSE_MESSAGE_KEY, AppConstants.JWT_ERROR_MALFORMED);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Map<String, String>> handleSignatureException(SignatureException e) {
        Map<String, String> response = new HashMap<>();
        response.put(AppConstants.RESPONSE_ERROR_KEY, AppConstants.JWT_ERROR_UNAUTHORIZED);
        response.put(AppConstants.RESPONSE_MESSAGE_KEY, AppConstants.JWT_ERROR_INVALID_SIGNATURE);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> response = new HashMap<>();
        response.put(AppConstants.RESPONSE_ERROR_KEY, AppConstants.JWT_ERROR_BAD_REQUEST);
        response.put(AppConstants.RESPONSE_MESSAGE_KEY, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}