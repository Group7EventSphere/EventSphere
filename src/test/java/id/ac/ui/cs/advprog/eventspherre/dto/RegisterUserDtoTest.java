package id.ac.ui.cs.advprog.eventspherre.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class RegisterUserDtoTest {

    @Test
    void defaultFieldsAreNull() {
        RegisterUserDto dto = new RegisterUserDto();
        assertNull(dto.getEmail(),    "email should be null by default");
        assertNull(dto.getPassword(), "password should be null by default");
        assertNull(dto.getName(),     "name should be null by default");
    }

    @Test
    void gettersReturnCorrectValues_afterReflectionSet() throws Exception {
        RegisterUserDto dto = new RegisterUserDto();

        // set private fields via reflection
        setPrivateField(dto, "email",    "test@example.com");
        setPrivateField(dto, "password", "password123");
        setPrivateField(dto, "name",     "Alice Wonderland");

        assertEquals("test@example.com",    dto.getEmail());
        assertEquals("password123",         dto.getPassword());
        assertEquals("Alice Wonderland",    dto.getName());
    }

    // helper to poke private fields
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
