package id.ac.ui.cs.advprog.eventspherre.exception;

import id.ac.ui.cs.advprog.eventspherre.model.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_shouldReturnErrorView() {
        NoHandlerFoundException exception = new NoHandlerFoundException("GET", "/nonexistent", null);
        
        String viewName = exceptionHandler.handleNotFound(request, exception);
        
        assertEquals("error", viewName);
    }
    
    @Test
    void handleUnauthorized_shouldReturnUnauthorizedView() {
        UnauthorizedAccessException exception = new UnauthorizedAccessException("Not authorized");
        
        String viewName = exceptionHandler.handleUnauthorized(request, exception);
        
        assertEquals("unauthorized", viewName);
    }
    
    @Test
    void handleAccessDenied_shouldReturnUnauthorizedView() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        
        String viewName = exceptionHandler.handleAccessDenied(request, exception);
        
        assertEquals("unauthorized", viewName);
    }
    
    @Test
    void handleServerError_shouldReturnErrorView() {
        Exception exception = new RuntimeException("Server error");
        
        String viewName = exceptionHandler.handleServerError(request, exception);
        
        assertEquals("error", viewName);
    }
}