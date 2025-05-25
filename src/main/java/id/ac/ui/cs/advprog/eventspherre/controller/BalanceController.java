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

    @ModelAttribute("currentUser")
    public User loadCurrentUser(Authentication auth) {
        return userService.getUserByEmail(auth.getName());
    }


    @GetMapping
    public String showPage(@ModelAttribute("currentUser") User user, Model model) {
        log.debug("Show balance page for userId={} " , user.getId());
        // Refresh user data to get latest balance
        User refreshedUser = userService.getUserById(user.getId());
        model.addAttribute("currentUser", refreshedUser);
        model.addAttribute("balance", refreshedUser.getBalance());
        model.addAttribute("userName", refreshedUser.getName());
        return "balance/topup";
    }

@PostMapping
public String topUp(@ModelAttribute("currentUser") User user,
                    @RequestParam double amount,
                    @RequestParam String method,
                    Model model) {
    log.info("Top‑up requested: userId={}, amount={}, method={}", user.getId(), amount, method);
    PaymentRequest req = new PaymentRequest(
        user,
        amount,
        PaymentRequest.PaymentType.TOPUP
    );
    req = requestRepo.save(req); 

    paymentHandler.handle(req);

    PaymentTransaction tx = paymentService.persistRequestAndConvert(req, "SUCCESS");
    User refreshed = userService.getUserById(user.getId());

    model.addAttribute("currentUser", refreshed);
    model.addAttribute("balance",  refreshed.getBalance());
    model.addAttribute("userName", refreshed.getName());
    model.addAttribute("flash",
        String.format("Top-up of %,d recorded successfully ✔", (long) tx.getAmount())
    );

    return "balance/topup";
}

    @GetMapping("/history")
    public String history(@ModelAttribute("currentUser") User user, Model model) {
        log.debug("Load balance history for userId={}", user.getId());
        // Refresh user data to get latest balance
        User refreshedUser = userService.getUserById(user.getId());
        List<PaymentRequest> reqs = requestRepo.findByUserId(refreshedUser.getId());
        model.addAttribute("currentUser", refreshedUser);
        model.addAttribute("requests", reqs);
        model.addAttribute("userName", refreshedUser.getName());
        model.addAttribute("balance", refreshedUser.getBalance());
        return "balance/history";
    }
}