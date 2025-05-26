package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class MainController {
    
    private final UserService userService;
    private final AdService adService;

    public MainController(UserService userService, AdService adService) {
        this.userService = userService;
        this.adService = adService;
    }
    
    @GetMapping("/")
    public String dashboard(Model model, Principal principal) {
        // Check if user is authenticated
        model.addAttribute("ads", adService.getAllAds());
        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            model.addAttribute("user", user);
        } else {
            // For non-authenticated users, create a default user for display purposes
            User guestUser = new User();
            guestUser.setName("Guest");
            guestUser.setEmail("Not logged in");
            guestUser.setBalance(AppConstants.DEFAULT_BALANCE);
            model.addAttribute("user", guestUser);
            model.addAttribute("isGuest", true);
        }
        return "dashboard";
    }
    
}
