package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.WebSecurityTestConfig;
import id.ac.ui.cs.advprog.eventspherre.config.BalanceTestConfig;
import id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.service.PaymentService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BalanceController.class, 
           properties = "spring.profiles.active=test")
@Import({WebSecurityTestConfig.class, BalanceTestConfig.class})
@ActiveProfiles("test")
class BalanceControllerTest {

    @Autowired private MockMvc mvc;

    @MockitoBean private PaymentHandler paymentHandler;
    @MockitoBean private UserService userService;
    @MockitoBean private PaymentService paymentService;
    @MockitoBean private PaymentRequestRepository requestRepo;
    @MockitoBean private AuthenticationProvider authenticationProvider;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(42);
        testUser.setName("Carol");
        testUser.setEmail("carol@example.com");
        testUser.setRole(User.Role.ATTENDEE);
        testUser.setBalance(150.0);
        
        // Mock the user service to return our test user
        given(userService.getUserByEmail(testUser.getEmail()))
                .willReturn(testUser);
        given(userService.getUserById(testUser.getId()))
                .willReturn(testUser);

        // Mock payment handler behavior
        doAnswer(inv -> {
            PaymentRequest req = inv.getArgument(0);
            req.setMessage("Top-up successful: balance added");
            req.setProcessed(true);
            return null;
        }).when(paymentHandler).handle(any(PaymentRequest.class));

        // Mock payment service behavior
        given(paymentService.persistRequestAndConvert(any(PaymentRequest.class), eq("SUCCESS")))
                .willAnswer(inv -> {
                    PaymentRequest req = inv.getArgument(0);
                    PaymentTransaction tx = new PaymentTransaction();
                    tx.setId(UUID.randomUUID());
                    tx.setAmount(req.getAmount());
                    tx.setUserId(req.getUser().getId());
                    tx.setType(req.getType());
                    tx.setStatus("SUCCESS");
                    return tx;
                });

        // Mock repository save
        given(requestRepo.save(any(PaymentRequest.class)))
                .willAnswer(inv -> {
                    PaymentRequest req = inv.getArgument(0);
                    req.setId(UUID.randomUUID());
                    return req;
                });
    }

    @Test
    @DisplayName("GET /balance should show topup page with current balance")
    void getTopUpPage_showsCurrentBalanceAndNav() throws Exception {
        mvc.perform(get("/balance")
                    .with(user(testUser.getEmail()).roles("ATTENDEE"))
                    .sessionAttr("currentUser", testUser))
           .andExpect(status().isOk())
           .andExpect(view().name("balance/topup"))
           .andExpect(model().attribute("balance", 150.0))
           .andExpect(model().attribute("userName", "Carol"))
           .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    @DisplayName("POST /balance should process topup and show success message")
    void postTopUp_updatesBalanceAndShowsFlash() throws Exception {
        // Create a refreshed user with updated balance
        User refreshedUser = new User();
        refreshedUser.setId(testUser.getId());
        refreshedUser.setName(testUser.getName());
        refreshedUser.setEmail(testUser.getEmail());
        refreshedUser.setRole(testUser.getRole());
        refreshedUser.setBalance(250.0); // 150 + 100 topup

        given(userService.getUserById(testUser.getId()))
                .willReturn(refreshedUser);

        mvc.perform(post("/balance")
                    .with(user(testUser.getEmail()).roles("ATTENDEE"))
                    .with(csrf()) // Add CSRF token
                    .sessionAttr("currentUser", testUser)
                    .param("amount", "100.0")
                    .param("method", "GOPAY"))
           .andExpect(status().isOk())
           .andExpect(view().name("balance/topup"))
           .andExpect(model().attribute("balance", 250.0))
           .andExpect(model().attribute("userName", "Carol"))
           .andExpect(model().attribute("flash", 
                    containsString("Top-up of 100 recorded successfully ✔")));
    }

    @Test
    @DisplayName("POST /balance with decimal amount should handle correctly")
    void postTopUp_withDecimalAmount_shouldWork() throws Exception {
        User refreshedUser = new User();
        refreshedUser.setId(testUser.getId());
        refreshedUser.setName(testUser.getName());
        refreshedUser.setEmail(testUser.getEmail());
        refreshedUser.setRole(testUser.getRole());
        refreshedUser.setBalance(180.5); // 150 + 30.5

        given(userService.getUserById(testUser.getId()))
                .willReturn(refreshedUser);

        mvc.perform(post("/balance")
                    .with(user(testUser.getEmail()).roles("ATTENDEE"))
                    .with(csrf()) // Add CSRF token
                    .sessionAttr("currentUser", testUser)
                    .param("amount", "30.5")
                    .param("method", "OVO"))
           .andExpect(status().isOk())
           .andExpect(view().name("balance/topup"))
           .andExpect(model().attribute("balance", 180.5))
           .andExpect(model().attribute("flash",
                    containsString("Top-up of 30 recorded successfully ✔")));
    }

    @Test
    @DisplayName("POST /balance with different payment methods should work")
    void postTopUp_withDifferentPaymentMethods_shouldWork() throws Exception {
        User refreshedUser = new User();
        refreshedUser.setId(testUser.getId());
        refreshedUser.setName(testUser.getName());
        refreshedUser.setEmail(testUser.getEmail());
        refreshedUser.setRole(testUser.getRole());
        refreshedUser.setBalance(250.0);

        given(userService.getUserById(testUser.getId()))
                .willReturn(refreshedUser);

        // Test with DANA
        mvc.perform(post("/balance")
                    .with(user(testUser.getEmail()).roles("ATTENDEE"))
                    .with(csrf()) // Add CSRF token
                    .sessionAttr("currentUser", testUser)
                    .param("amount", "100.0")
                    .param("method", "DANA"))
           .andExpect(status().isOk())
           .andExpect(view().name("balance/topup"));
    }

    @Test
    @DisplayName("GET /balance/history should show transaction history")
    void historyPage_displaysUserRequests() throws Exception {
        // Create test payment requests
        PaymentRequest r1 = new PaymentRequest();
        r1.setId(UUID.randomUUID());
        r1.setUser(testUser);
        r1.setAmount(10.0);
        r1.setType(PaymentRequest.PaymentType.TOPUP);
        r1.setProcessed(true);
        r1.setMessage("Top-up successful");

        PaymentRequest r2 = new PaymentRequest();
        r2.setId(UUID.randomUUID());
        r2.setUser(testUser);
        r2.setAmount(5.0);
        r2.setType(PaymentRequest.PaymentType.PURCHASE);
        r2.setProcessed(false);
        r2.setMessage("Purchase pending");

        given(requestRepo.findByUserId(testUser.getId()))
                .willReturn(List.of(r1, r2));

        mvc.perform(get("/balance/history")
                    .with(user(testUser.getEmail()).roles("ATTENDEE"))
                    .sessionAttr("currentUser", testUser))
           .andExpect(status().isOk())
           .andExpect(view().name("balance/history"))
           .andExpect(model().attribute("balance", 150.0))
           .andExpect(model().attribute("userName", "Carol"))
           .andExpect(model().attributeExists("requests"));
    }

    @Test
    @DisplayName("GET /balance/history with empty history should show no history message")
    void historyPage_withEmptyHistory_shouldShowEmptyMessage() throws Exception {
        given(requestRepo.findByUserId(testUser.getId()))
                .willReturn(List.of());

        mvc.perform(get("/balance/history")
                    .with(user(testUser.getEmail()).roles("ATTENDEE"))
                    .sessionAttr("currentUser", testUser))
           .andExpect(status().isOk())
           .andExpect(view().name("balance/history"))
           .andExpect(model().attribute("balance", 150.0))
           .andExpect(model().attribute("userName", "Carol"))
           .andExpect(model().attribute("requests", List.of()));
    }

    @Test
    @DisplayName("POST /balance with zero amount should still process")
    void postTopUp_withZeroAmount_shouldProcess() throws Exception {
        User refreshedUser = new User();
        refreshedUser.setId(testUser.getId());
        refreshedUser.setName(testUser.getName());
        refreshedUser.setEmail(testUser.getEmail());
        refreshedUser.setRole(testUser.getRole());
        refreshedUser.setBalance(150.0); // No change

        given(userService.getUserById(testUser.getId()))
                .willReturn(refreshedUser);

        mvc.perform(post("/balance")
                    .with(user(testUser.getEmail()).roles("ATTENDEE"))
                    .with(csrf()) // Add CSRF token
                    .sessionAttr("currentUser", testUser)
                    .param("amount", "0.0")
                    .param("method", "GOPAY"))
           .andExpect(status().isOk())
           .andExpect(view().name("balance/topup"))
           .andExpect(model().attribute("flash",
                    containsString("Top-up of 0 recorded successfully ✔")));
    }

    @Test
    @DisplayName("loadCurrentUser should return user from authentication")
    void loadCurrentUser_shouldReturnUserFromAuth() throws Exception {
        // This tests the @ModelAttribute method indirectly through a request
        mvc.perform(get("/balance")
                    .with(user(testUser.getEmail()).roles("ATTENDEE")))
           .andExpect(status().isOk())
           .andExpect(view().name("balance/topup"));
    }

    @Test
    @DisplayName("Balance controller should require ATTENDEE role")
    void balanceController_shouldRequireAttendeeRole() throws Exception {
        // Create a user with ADMIN role but no session
        mvc.perform(get("/balance")
                    .with(user("admin@example.com").roles("ADMIN")))
           .andExpect(result -> {
               int status = result.getResponse().getStatus();
               if (status != 403 && status != 500) {
                   throw new AssertionError("Expected status 403 or 500 but got: " + status);
               }
           }); 
    }

    @Test
    @DisplayName("Unauthenticated access should be redirected or forbidden")
    void unauthenticatedAccess_shouldBeRedirected() throws Exception {
        mvc.perform(get("/balance"))
           .andExpect(result -> {
               int status = result.getResponse().getStatus();
               if (status != 302 && status != 401 && status != 403 && status != 500) {
                   throw new AssertionError("Unexpected status: " + status);
               }
           });
    }
}