package id.ac.ui.cs.advprog.eventspherre.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class LoginUserDtoTest {

    @Test
    void defaultFieldsAreNull() {
        LoginUserDto dto = new LoginUserDto();
        assertNull(dto.getEmail(),    "email should be null by default");
        assertNull(dto.getPassword(), "password should be null by default");
    }

    @Test
    void gettersReturnCorrectValues_afterReflectionSet() throws Exception {
        LoginUserDto dto = new LoginUserDto();

        // inject test data into private fields
        setPrivateField(dto, "email",    "foo@bar.com");
        setPrivateField(dto, "password", "s3cr3t");

        assertEquals("foo@bar.com", dto.getEmail());
        assertEquals("s3cr3t",      dto.getPassword());
    }

    // helper to set private fields via reflection
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
