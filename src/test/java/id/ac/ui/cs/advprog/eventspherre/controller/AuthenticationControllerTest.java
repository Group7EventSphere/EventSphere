package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.dto.RegisterUserDto;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.AuthenticationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private Model model;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void testLoginPage() {
        // Act
        String result = authenticationController.loginPage();
        
        // Assert
        assertEquals("login", result);
    }

    @Test
    void testRegisterPage() {
        // Act
        String result = authenticationController.registerPage(model);
        
        // Assert
        verify(model).addAttribute(eq("registerDto"), any(RegisterUserDto.class));
        assertEquals("register", result);
    }

    @Test
    void testRegisterSubmit() {
        // Arrange
        RegisterUserDto dto = new RegisterUserDto();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setPhoneNumber("1234567890");

        User user = new User();
        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(user);

        // Act
        String result = authenticationController.register(dto);

        // Assert
        verify(authenticationService).signup(dto);
        assertEquals("redirect:/login", result);
    }


    private void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + " but was: " + actual);
        }
    }
}