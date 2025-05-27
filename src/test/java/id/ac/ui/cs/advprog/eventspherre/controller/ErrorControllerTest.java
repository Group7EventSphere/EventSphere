package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.WebSecurityTestConfig;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ErrorController.class)
@Import(WebSecurityTestConfig.class)
@ActiveProfiles("test")
class ErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testUnauthorizedEndpoint() throws Exception {
        mockMvc.perform(get("/unauthorized"))
                .andExpect(status().isOk())
                .andExpect(view().name(AppConstants.VIEW_UNAUTHORIZED));
    }

    @Test
    void testHandleError404() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
                .andExpect(status().isOk())
                .andExpect(view().name(AppConstants.VIEW_ERROR))
                .andExpect(model().attribute("errorMessage", AppConstants.ERROR_PAGE_NOT_FOUND))
                .andExpect(model().attribute("errorCode", "404"));
    }

    @Test
    void testHandleError403() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403))
                .andExpect(status().isOk())
                .andExpect(view().name(AppConstants.VIEW_UNAUTHORIZED));
        // No model attributes expected
    }

    @Test
    void testHandleError500() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500))
                .andExpect(status().isOk())
                .andExpect(view().name(AppConstants.VIEW_ERROR))
                .andExpect(model().attribute("errorMessage", AppConstants.ERROR_INTERNAL_SERVER))
                .andExpect(model().attribute("errorCode", "500"));
    }

    @Test
    void testHandleErrorGeneric() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400))
                .andExpect(status().isOk())
                .andExpect(view().name(AppConstants.VIEW_ERROR));
        // No model attributes expected
    }

    @Test
    void testHandleErrorNoStatusCode() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(view().name(AppConstants.VIEW_ERROR))
                .andExpect(model().attribute("errorMessage", AppConstants.ERROR_UNEXPECTED));
        // No errorCode expected
    }

    @Test
    void testHandleErrorWithNullStatusCode() throws Exception {
    mockMvc.perform(get("/error"))
            .andExpect(status().isOk())
            .andExpect(view().name(AppConstants.VIEW_ERROR))
            .andExpect(model().attribute("errorMessage", AppConstants.ERROR_UNEXPECTED));
    // errorCode is not set by controller in this path
    }
}
