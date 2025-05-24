package id.ac.ui.cs.advprog.eventspherre.observer;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class EventSubjectTest {

    private EventSubject eventSubject;

    @Mock
    private EventObserver observer1;

    @Mock
    private EventObserver observer2;

    @Mock
    private Event event;

    @BeforeEach
    public void setUp() {
        eventSubject = new EventSubject();
    }

    @Test
    public void testAddObserver() {
        // Add observer and notify
        eventSubject.addObserver(observer1);
        eventSubject.notifyEventCreated(event);

        // Verify observer was notified
        verify(observer1, times(1)).onEventCreated(event);
    }

    @Test
    public void testRemoveObserver() {
        // Add observer
        eventSubject.addObserver(observer1);

        // Remove observer
        eventSubject.removeObserver(observer1);

        // Notify
        eventSubject.notifyEventCreated(event);

        // Verify observer was not notified
        verify(observer1, never()).onEventCreated(event);
    }

    @Test
    public void testNotifyEventCreated() {
        // Add observers
        eventSubject.addObserver(observer1);
        eventSubject.addObserver(observer2);

        // Notify
        eventSubject.notifyEventCreated(event);

        // Verify observers were notified
        verify(observer1).onEventCreated(event);
        verify(observer2).onEventCreated(event);
    }

    @Test
    public void testNotifyEventUpdated() {
        // Add observers
        eventSubject.addObserver(observer1);
        eventSubject.addObserver(observer2);

        // Notify
        eventSubject.notifyEventUpdated(event);

        // Verify observers were notified
        verify(observer1).onEventUpdated(event);
        verify(observer2).onEventUpdated(event);
    }

    @Test
    public void testNotifyEventVisibilityChanged() {
        // Add observers
        eventSubject.addObserver(observer1);
        eventSubject.addObserver(observer2);

        // Notify
        boolean isPublic = true;
        eventSubject.notifyEventVisibilityChanged(event, isPublic);

        // Verify observers were notified
        verify(observer1).onEventVisibilityChanged(event, isPublic);
        verify(observer2).onEventVisibilityChanged(event, isPublic);
    }

    @Test
    public void testNotifyEventDeleted() {
        // Add observers
        eventSubject.addObserver(observer1);
        eventSubject.addObserver(observer2);

        // Notify
        eventSubject.notifyEventDeleted(event);

        // Verify observers were notified
        verify(observer1).onEventDeleted(event);
        verify(observer2).onEventDeleted(event);
    }
}
