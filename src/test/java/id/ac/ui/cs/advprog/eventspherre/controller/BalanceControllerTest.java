package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.SecurityConfiguration;
import id.ac.ui.cs.advprog.eventspherre.config.PaymentChainConfig;
import id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler;
import id.ac.ui.cs.advprog.eventspherre.handler.TopUpHandler;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.service.PaymentService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
@Import(SecurityConfiguration.class)
class BalanceControllerTest {

    @Autowired private MockMvc mvc;

    @MockBean private PaymentHandler           paymentHandler;
    @MockBean private UserService              userService;
    @MockBean private PaymentService           paymentService;
    @MockBean private PaymentRequestRepository requestRepo;
    @MockBean private AuthenticationProvider   authenticationProvider;

    private User normalUser;

    @BeforeEach
    void setUp() {
        normalUser = new User();
        normalUser.setId(42);
        normalUser.setName("Carol");
        normalUser.setEmail("carol@example.com");
        normalUser.setRole(User.Role.ATTENDEE);
        normalUser.setBalance(0.0);

        given(userService.getUserByEmail(normalUser.getEmail()))
                .willReturn(normalUser);
        given(userService.getUserById(normalUser.getId()))
                .willAnswer(inv -> normalUser);

        doAnswer(inv -> {
            PaymentRequest req = inv.getArgument(0);
            User u = req.getUser();
            u.setBalance(u.getBalance() + req.getAmount());
            req.setMessage("Top-up successful: balance added");
            return null;
        }).when(paymentHandler).handle(any(PaymentRequest.class));

        given(paymentService.persistRequestAndConvert(any(PaymentRequest.class), eq("SUCCESS")))
                .willAnswer(inv -> {
                    PaymentRequest req = inv.getArgument(0);
                    PaymentTransaction tx = new PaymentTransaction();
                    tx.setAmount(req.getAmount());
                    return tx;
                });

        given(requestRepo.save(any(PaymentRequest.class)))
                .willAnswer(inv -> inv.getArgument(0));
    }

 
    @Test
    void getTopUpPage_showsCurrentBalanceAndNav() throws Exception {
        normalUser.setBalance(150.0);

        mvc.perform(get("/balance")
                    .with(user(normalUser.getEmail())
                          .roles(normalUser.getRole().name()))
                    .sessionAttr("currentUser", normalUser))
           .andExpect(status().isOk())
           .andExpect(view().name("topup"))
           .andExpect(model().attribute("balance", 150.0))
           .andExpect(model().attribute("userName", "Carol"))
           .andExpect(content().string(containsString("Carol")))
           .andExpect(content().string(containsString("150.0")));
    }

    @Test
    void postTopUp_updatesBalanceAndShowsFlash() throws Exception {
        normalUser.setBalance(20.0);
        normalUser.setName("Bob");
        normalUser.setEmail("bob@example.com");

        mvc.perform(post("/balance")
                    .with(user(normalUser.getEmail())
                          .roles(normalUser.getRole().name()))
                    .sessionAttr("currentUser", normalUser)
                    .param("amount", "30.5")
                    .param("method", "Gopay"))
           .andExpect(status().isOk())
           .andExpect(view().name("topup"))
           .andExpect(model().attribute("balance", 50.5))   // 20 + 30.5
           .andExpect(model().attribute("userName", "Bob"))
           .andExpect(model().attribute("flash",
                    "Top-up of 30 recorded successfully ✔"))
           .andExpect(content().string(containsString("50.5")));
    }

    @Test
    void historyPage_displaysUserRequests() throws Exception {
        var r1 = PaymentRequest.builder()
                .userId(normalUser.getId()).amount(10.0)
                .type(PaymentRequest.PaymentType.TOPUP)
                .processed(true).message("OK").build();
        var r2 = PaymentRequest.builder()
                .userId(normalUser.getId()).amount(5.0)
                .type(PaymentRequest.PaymentType.PURCHASE)
                .processed(false).message("FAIL").build();

        given(requestRepo.findByUserId(normalUser.getId()))
                .willReturn(List.of(r1, r2));

        mvc.perform(get("/balance/history")
                    .with(user(normalUser.getEmail())
                          .roles(normalUser.getRole().name()))
                    .sessionAttr("currentUser", normalUser))
           .andExpect(status().isOk())
           .andExpect(view().name("history"))
           .andExpect(model().attribute("userName", "Carol"))
           .andExpect(model().attributeExists("requests"))
           .andExpect(content().string(containsString("OK")))
           .andExpect(content().string(containsString("FAIL")));
    }
}
