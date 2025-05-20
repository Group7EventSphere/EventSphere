package id.ac.ui.cs.advprog.eventspherre.controller;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PaymentChainConfig paymentChainConfig;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentRequestRepository requestRepo;

    private ExecutorService mockExec;
    private PaymentHandler  mockChain;

    @BeforeEach
    void setUp() throws InterruptedException {
        // 1) stub executor
        mockExec = mock(ExecutorService.class);
        given(mockExec.awaitTermination(anyLong(), any(TimeUnit.class))).willReturn(true);

        // 2) stub handler chain to do an in-memory top-up
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

        // 3) stub paymentService to echo back a transaction with the same amount
        given(paymentService.persistRequestAndConvert(any(PaymentRequest.class), eq("SUCCESS")))
            .willAnswer(inv -> {
                PaymentRequest  req = inv.getArgument(0);
                PaymentTransaction tx = new PaymentTransaction();
                tx.setAmount(req.getAmount());
                return tx;
            });
    }

    @Test
    void getTopUpPage_showsCurrentBalanceAndNav() throws Exception {
        User user = new User();
        user.setId(1);
        user.setName("Alice");
        user.setBalance(150.0);

        mvc.perform(get("/balance")
                .sessionAttr("currentUser", user))
           .andExpect(status().isOk())
           .andExpect(view().name("topup"))
           .andExpect(model().attribute("balance",  150.0))
           .andExpect(model().attribute("userName", "Alice"))
           .andExpect(content().string(containsString("EventSphere")))
           .andExpect(content().string(containsString("Alice")))
           .andExpect(content().string(containsString("150.0")));
    }

    @Test
    void postTopUp_updatesBalanceAndShowsFlash() throws Exception {
        User user = new User();
        user.setId(2);
        user.setName("Bob");
        user.setBalance(20.0);

        mvc.perform(post("/balance")
                .sessionAttr("currentUser", user)
                .param("amount", "30.5")
                .param("method", "Gopay"))
           .andExpect(status().isOk())
           .andExpect(view().name("topup"))
           .andExpect(model().attribute("balance", 50.5))
           .andExpect(model().attribute("userName", "Bob"))
           .andExpect(model().attribute("flash",
                   "Top-up recorded: 30.5 saved"))
           .andExpect(content().string(
                   containsString("Top-up recorded: 30.5 saved")))
           .andExpect(content().string(containsString("50.5")));
    }

    @Test
    void postTopUp_appliesAmountAndShowsFlash() throws Exception {
        User bob = new User();
        bob.setId(11);
        bob.setName("Bob");
        bob.setBalance(50.0);

        mvc.perform(post("/balance")
                        .sessionAttr("currentUser", bob)
                        .param("amount", "25.5")
                        .param("method", "Gopay"))
                .andExpect(status().isOk())
                .andExpect(view().name("topup"))
                .andExpect(model().attribute("balance", 75.5))
                .andExpect(model().attribute("userName", "Bob"))
                .andExpect(model().attribute("flash", "Top-up recorded: 25.5 saved"))
                .andExpect(content().string(containsString("75.5")))
                .andExpect(content().string(containsString("Top-up recorded: 25.5 saved")));
    }

    @Test
    void historyPage_displaysUserRequests() throws Exception {
        User carol = new User();
        carol.setId(12);
        carol.setName("Carol");

        PaymentRequest r1 = PaymentRequest.builder()
                .userId(12)
                .amount(10.0)
                .type(PaymentRequest.PaymentType.TOPUP)
                .processed(true)
                .message("OK")
                .createdAt(Instant.parse("2025-05-01T00:00:00Z"))
                .build();
        PaymentRequest r2 = PaymentRequest.builder()
                .userId(12)
                .amount(5.0)
                .type(PaymentRequest.PaymentType.PURCHASE)
                .processed(false)
                .message("FAIL")
                .createdAt(Instant.parse("2025-05-02T00:00:00Z"))
                .build();

        given(requestRepo.findByUserId(12)).willReturn(List.of(r1, r2));

        mvc.perform(get("/balance/history")
                        .sessionAttr("currentUser", carol))
                .andExpect(status().isOk())
                .andExpect(view().name("history"))
                .andExpect(model().attribute("userName", "Carol"))
                .andExpect(model().attributeExists("requests"))
                .andExpect(content().string(containsString("OK")))
                .andExpect(content().string(containsString("FAIL")));
    }
}
