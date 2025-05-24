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

import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ATTENDEE')")
@RequestMapping("/balance")
@SessionAttributes("currentUser")
public class BalanceController {

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
        model.addAttribute("balance", user.getBalance());
        model.addAttribute("userName", user.getName());
        return "topup";
    }

@PostMapping
public String topUp(@ModelAttribute("currentUser") User user,
                    @RequestParam double amount,
                    @RequestParam String method,
                    Model model) {

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

    return "topup";
}

    @GetMapping("/history")
    public String history(@ModelAttribute("currentUser") User user, Model model) {
        List<PaymentRequest> reqs = requestRepo.findByUserId(user.getId());
        model.addAttribute("requests", reqs);
        model.addAttribute("userName",  user.getName());
        return "history";
    }
}