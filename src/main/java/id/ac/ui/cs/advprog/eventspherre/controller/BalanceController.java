package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.PaymentChainConfig;
import id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PaymentRequestRepository;
import id.ac.ui.cs.advprog.eventspherre.service.PaymentService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ExecutorService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ATTENDEE')")
@RequestMapping("/balance")
public class BalanceController {
    private final PaymentChainConfig paymentChainConfig;
    private final PaymentService     paymentService;
    private final PaymentRequestRepository requestRepo;

    @GetMapping
    public String showPage(Model model, @SessionAttribute User currentUser) {
        model.addAttribute("balance", currentUser.getBalance());
        model.addAttribute("userName", currentUser.getName());
        return "topup";
    }

    @PostMapping
    public String topUp(@SessionAttribute User currentUser,
                        @RequestParam double amount,
                        @RequestParam String method,
                        Model model) {
        ExecutorService exec  = paymentChainConfig.paymentExecutor();
        PaymentHandler  chain = paymentChainConfig.paymentHandlerChain(exec);

        PaymentRequest req = new PaymentRequest(currentUser, amount, PaymentRequest.PaymentType.TOPUP);
        chain.handle(req);

        PaymentTransaction tx = paymentService.persistRequestAndConvert(req, "SUCCESS");

        model.addAttribute("balance", currentUser.getBalance());
        model.addAttribute("userName", currentUser.getName());
        model.addAttribute("flash", "Top-up recorded: " + tx.getAmount() + " saved");
        return "topup";
    }

    @GetMapping("/history")
    public String history(Model model, @SessionAttribute User currentUser) {
        model.addAttribute("userName", currentUser.getName());
        List<PaymentRequest> reqs = requestRepo.findByUserId(currentUser.getId());
        model.addAttribute("requests", reqs);
        return "history";
    }
}