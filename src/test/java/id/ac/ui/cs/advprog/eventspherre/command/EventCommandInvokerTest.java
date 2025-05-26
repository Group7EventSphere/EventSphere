package id.ac.ui.cs.advprog.eventspherre.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventCommandInvokerTest {

    private EventCommandInvoker commandInvoker;

    @Mock
    private EventCommand mockCommand1;

    @Mock
    private EventCommand mockCommand2;

    @BeforeEach
    public void setUp() {
        commandInvoker = new EventCommandInvoker();
    }

    @Test
    public void testExecuteCommand() {
        // Act
        commandInvoker.executeCommand(mockCommand1);

        // Assert
        verify(mockCommand1, times(1)).execute();
    }

    @Test
    public void testUndoLastCommand() {
        // Arrange
        commandInvoker.executeCommand(mockCommand1);
        commandInvoker.executeCommand(mockCommand2);

        // Act
        commandInvoker.undoLastCommand();

        // Assert
        verify(mockCommand2, times(1)).undo();
        verify(mockCommand1, never()).undo(); // First command should not be undone
    }

    @Test
    public void testUndoLastCommandEmptyStack() {
        // Act
        commandInvoker.undoLastCommand();

        // Assert - no exceptions should be thrown
        assertTrue(true);
    }

    @Test
    public void testHasCommandHistory() {
        // Assert - initially empty
        assertFalse(commandInvoker.hasCommandHistory());

        // Act
        commandInvoker.executeCommand(mockCommand1);

        // Assert - now has history
        assertTrue(commandInvoker.hasCommandHistory());

        // Act
        commandInvoker.undoLastCommand();

        // Assert - empty again
        assertFalse(commandInvoker.hasCommandHistory());
    }

    @Test
    public void testClearCommandHistory() {
        // Arrange
        commandInvoker.executeCommand(mockCommand1);
        commandInvoker.executeCommand(mockCommand2);
        assertTrue(commandInvoker.hasCommandHistory());

        // Act
        commandInvoker.clearCommandHistory();

        // Assert
        assertFalse(commandInvoker.hasCommandHistory());

        // Further undo operations should not affect commands
        commandInvoker.undoLastCommand();
        verify(mockCommand1, never()).undo();
        verify(mockCommand2, never()).undo();
    }
}
