package id.ac.ui.cs.advprog.eventspherre.observer;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserObserverTest {

    @Mock
    private Event event;

    private UserObserver userObserver;
    private final String username = "testUser";
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        userObserver = new UserObserver(username);
        
        // Capture System.out output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        // Setup mock event
        when(event.getId()).thenReturn(1);
        when(event.getTitle()).thenReturn("Test Event");
    }

    @Test
    void testOnEventCreated() {
        // Act
        userObserver.onEventCreated(event);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains(username + " received notification: Event created - Test Event (ID: 1)"));
        
        // Restore System.out
        System.setOut(originalOut);
    }

    @Test
    void testOnEventUpdated() {
        // Act
        userObserver.onEventUpdated(event);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains(username + " received notification: Event updated - Test Event (ID: 1)"));
        
        // Restore System.out
        System.setOut(originalOut);
    }

    @Test
    void testOnEventVisibilityChangedToPublic() {
        // Act
        userObserver.onEventVisibilityChanged(event, true);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains(username + " received notification: Event visibility changed to public - Test Event (ID: 1)"));
        
        // Restore System.out
        System.setOut(originalOut);
    }

    @Test
    void testOnEventVisibilityChangedToPrivate() {
        // Act
        userObserver.onEventVisibilityChanged(event, false);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains(username + " received notification: Event visibility changed to private - Test Event (ID: 1)"));
        
        // Restore System.out
        System.setOut(originalOut);
    }

    @Test
    void testOnEventDeleted() {
        // Act
        userObserver.onEventDeleted(event);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains(username + " received notification: Event deleted - Test Event (ID: 1)"));
        
        // Restore System.out
        System.setOut(originalOut);
    }

    @Test
    void testConstructorSetsUsername() {
        // Arrange
        String testUsername = "newUser";
        
        // Act
        UserObserver observer = new UserObserver(testUsername);
        
        // Assert
        // Since username is private, we test it indirectly through the notification methods
        observer.onEventCreated(event);
        String output = outputStream.toString();
        assertTrue(output.contains(testUsername + " received notification"));
        
        // Restore System.out
        System.setOut(originalOut);
    }
}
