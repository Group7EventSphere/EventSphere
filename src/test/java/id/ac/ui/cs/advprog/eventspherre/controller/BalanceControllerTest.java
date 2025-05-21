package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.SecurityConfiguration;
import id.ac.ui.cs.advprog.eventspherre.config.PaymentChainConfig;
import id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.service.PaymentService;
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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
@Import(SecurityConfiguration.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean private PaymentChainConfig         paymentChainConfig;
    @MockBean private PaymentService             paymentService;
    @MockBean private PaymentRequestRepository   requestRepo;
    @MockBean private AuthenticationProvider     authenticationProvider; // satisfy SecurityConfiguration

    private ExecutorService mockExec;
    private PaymentHandler  mockChain;
    private User            normalUser;

    @BeforeEach
    void setUp() throws  InterruptedException  {
        // 1) forge a normal user (ATTENDEE)
        normalUser = new User();
        normalUser.setId(42);
        normalUser.setName("Carol");
        normalUser.setEmail("carol@example.com");
        normalUser.setRole(User.Role.ATTENDEE);

        // 2) stub executor so chain.awaitTermination(...) is happy
        mockExec = mock(ExecutorService.class);
        given(mockExec.awaitTermination(anyLong(), any(TimeUnit.class))).willReturn(true);

        // 3) stub the handler chain to actually bump the user's balance
        mockChain = new PaymentHandler() {
            @Override public void setNext(PaymentHandler next) {}
            @Override public void handle(PaymentRequest req) {
                User u = req.getUser();
                u.setBalance(u.getBalance() + req.getAmount());
                req.setMessage("Top-up successful: balance added");
            }
        };
        given(paymentChainConfig.paymentExecutor()).willReturn(mockExec);
        given(paymentChainConfig.paymentHandlerChain(mockExec)).willReturn(mockChain);

        // 4) stub paymentService to echo back a successful transaction
        given(paymentService.persistRequestAndConvert(any(PaymentRequest.class), eq("SUCCESS")))
                .willAnswer(inv -> {
                    PaymentRequest req = inv.getArgument(0);
                    PaymentTransaction tx = new PaymentTransaction();
                    tx.setAmount(req.getAmount());
                    return tx;
                });
    }

    @Test
    void getTopUpPage_showsCurrentBalanceAndNav() throws Exception {
        // put 150.0 on Carol’s account
        normalUser.setBalance(150.0);

        mvc.perform(get("/balance")
                        .sessionAttr("currentUser", normalUser)
                        .with(user(normalUser.getEmail())
                                .roles(normalUser.getRole().name()))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("topup"))
                .andExpect(model().attribute("balance", 150.0))
                .andExpect(model().attribute("userName", "Carol"))
                .andExpect(content().string(containsString("Carol")))
                .andExpect(content().string(containsString("150.0")));
    }

    @Test
    void postTopUp_updatesBalanceAndShowsFlash() throws Exception {
        // start Bob (reuse normalUser object)
        normalUser.setBalance(20.0);
        normalUser.setName("Bob");
        normalUser.setEmail("bob@example.com");

        mvc.perform(post("/balance")
                        .sessionAttr("currentUser", normalUser)
                        .with(user(normalUser.getEmail())
                                .roles(normalUser.getRole().name()))
                        .param("amount", "30.5")
                        .param("method", "Gopay")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("topup"))
                .andExpect(model().attribute("balance", 50.5))
                .andExpect(model().attribute("userName", "Bob"))
                .andExpect(model().attribute("flash", "Top-up recorded: 30.5 saved"))
                .andExpect(content().string(containsString("50.5")))
                .andExpect(content().string(containsString("Top-up recorded: 30.5 saved")));
    }

    @Test
    void historyPage_displaysUserRequests() throws Exception {
        // Carol again
        normalUser.setName("Carol");
        normalUser.setEmail("carol@example.com");

        var r1 = PaymentRequest.builder()
                .userId(normalUser.getId())
                .amount(10.0)
                .type(PaymentRequest.PaymentType.TOPUP)
                .processed(true)
                .message("OK")
                .createdAt(Instant.parse("2025-05-01T00:00:00Z"))
                .build();
        var r2 = PaymentRequest.builder()
                .userId(normalUser.getId())
                .amount(5.0)
                .type(PaymentRequest.PaymentType.PURCHASE)
                .processed(false)
                .message("FAIL")
                .createdAt(Instant.parse("2025-05-02T00:00:00Z"))
                .build();

        given(requestRepo.findByUserId(normalUser.getId()))
                .willReturn(List.of(r1, r2));

        mvc.perform(get("/balance/history")
                        .sessionAttr("currentUser", normalUser)
                        .with(user(normalUser.getEmail())
                                .roles(normalUser.getRole().name()))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("history"))
                .andExpect(model().attribute("userName", "Carol"))
                .andExpect(model().attributeExists("requests"))
                .andExpect(content().string(containsString("OK")))
                .andExpect(content().string(containsString("FAIL")));
    }
}
