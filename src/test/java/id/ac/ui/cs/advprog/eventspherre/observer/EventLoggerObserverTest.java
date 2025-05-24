package id.ac.ui.cs.advprog.eventspherre.observer;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventLoggerObserverTest {

    private EventLoggerObserver eventLoggerObserver;

    @Mock
    private Logger logger;

    @Mock
    private Event event;

    @BeforeEach
    public void setUp() {
        eventLoggerObserver = new EventLoggerObserver();

        // Use reflection to set the mocked logger
        try {
            java.lang.reflect.Field loggerField = EventLoggerObserver.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set(eventLoggerObserver, logger);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup event mock
        when(event.getId()).thenReturn(1);
        when(event.getTitle()).thenReturn("Test Event");
    }

    @Test
    public void testOnEventCreated() {
        // Act
        eventLoggerObserver.onEventCreated(event);

        // Assert
        verify(logger).info(contains("Event created"), eq("Test Event"), eq(1));
    }

    @Test
    public void testOnEventUpdated() {
        // Act
        eventLoggerObserver.onEventUpdated(event);

        // Assert
        verify(logger).info(contains("Event updated"), eq("Test Event"), eq(1));
    }

    @Test
    public void testOnEventVisibilityChanged() {
        // Act - test with visibility set to true
        eventLoggerObserver.onEventVisibilityChanged(event, true);

        // Assert
        verify(logger).info(contains("Event visibility changed"), eq("Test Event"), eq(1), eq("public"));

        // Reset mock
        reset(logger);

        // Act - test with visibility set to false
        eventLoggerObserver.onEventVisibilityChanged(event, false);

        // Assert
        verify(logger).info(contains("Event visibility changed"), eq("Test Event"), eq(1), eq("private"));
    }

    @Test
    public void testOnEventDeleted() {
        // Act
        eventLoggerObserver.onEventDeleted(event);

        // Assert
        verify(logger).info(contains("Event deleted"), eq("Test Event"), eq(1));
    }

    @Test
    public void testTestingConstructor() {
        // Act
        EventLoggerObserver observer = new EventLoggerObserver(logger);

        // Setup event mock
        when(event.getId()).thenReturn(1);
        when(event.getTitle()).thenReturn("Test Event");

        // Act
        observer.onEventCreated(event);

        // Assert
        verify(logger).info(contains("Event created"), eq("Test Event"), eq(1));
    }
}
