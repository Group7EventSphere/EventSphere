package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler;
import id.ac.ui.cs.advprog.eventspherre.model.*;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.service.PaymentService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.support.SessionStatus;


import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ATTENDEE')")
@RequestMapping("/balance")
@SessionAttributes("currentUser")
public class BalanceController {

    private static final Logger log = LoggerFactory.getLogger(BalanceController.class);

    private final PaymentHandler          paymentHandler;
    private final PaymentService          paymentService;
    private final PaymentRequestRepository requestRepo;
    private final UserService             userService;

    private static final String CURRENT_USER = "currentUser";
    private static final String BALANCE = "balance";
    private static final String USER_NAME = "userName";

    @ModelAttribute(CURRENT_USER)
    public User loadCurrentUser(Authentication auth) {
        return userService.getUserByEmail(auth.getName());
    }


    @GetMapping
    public String showPage(@ModelAttribute(CURRENT_USER) User user, Model model) {
        log.debug("Show balance page for userId={} " , user.getId());
        // Refresh user data to get latest balance
        User refreshedUser = userService.getUserById(user.getId());
        model.addAttribute(CURRENT_USER, refreshedUser);
        model.addAttribute(BALANCE, refreshedUser.getBalance());
        model.addAttribute(USER_NAME, refreshedUser.getName());
        return "balance/topup";
    }

@PostMapping
public String topUp(@ModelAttribute(CURRENT_USER) User user,
                    @RequestParam double amount,
                    @RequestParam String method,
                    Model model,
                    SessionStatus status) {
    log.info("Top‑up requested: amount={}", amount);
    PaymentRequest req = new PaymentRequest(
        user,
        amount,
        PaymentRequest.PaymentType.TOPUP
    );
    req = requestRepo.save(req); 

    paymentHandler.handle(req);

    PaymentTransaction tx = paymentService.persistRequestAndConvert(req, "SUCCESS");
    User refreshed = userService.getUserById(user.getId());

    model.addAttribute(CURRENT_USER, refreshed);
    model.addAttribute(BALANCE,  refreshed.getBalance());
    model.addAttribute(USER_NAME, refreshed.getName());
    model.addAttribute("flash",
        String.format("Top-up of %,d recorded successfully ✔", (long) tx.getAmount())
    );
    status.setComplete();
    return "balance/topup";
}

    @GetMapping("/history")
    public String history(@ModelAttribute(CURRENT_USER) User user, Model model) {
        log.debug("Load balance history for userId={}", user.getId());
        // Refresh user data to get latest balance
        User refreshedUser = userService.getUserById(user.getId());
        List<PaymentRequest> reqs = requestRepo.findByUserId(refreshedUser.getId());
        model.addAttribute(CURRENT_USER, refreshedUser);
        model.addAttribute("requests", reqs);
        model.addAttribute(USER_NAME, refreshedUser.getName());
        model.addAttribute(BALANCE, refreshedUser.getBalance());
        return "balance/history";
    }
}