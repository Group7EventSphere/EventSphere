package id.ac.ui.cs.advprog.eventspherre.model;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);  // Pass the message to the superclass constructor
    }
}
