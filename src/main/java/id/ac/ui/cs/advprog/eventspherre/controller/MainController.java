package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class MainController {
    
    private final UserService userService;
    private final EventManagementService eventManagementService;
    private final AdService adService;

    public MainController(UserService userService, EventManagementService eventManagementService, AdService adService) {
        this.userService = userService;
        this.eventManagementService = eventManagementService;
        this.adService = adService;
    }
      @GetMapping("/")
    public String dashboard(Model model, Principal principal) {
        // Check if user is authenticated
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
        
        // Get top 5 recent public events
        List<Event> allPublicEvents = eventManagementService.findPublicEvents();
        List<Event> recentEvents = allPublicEvents.stream()
                .sorted((e1, e2) -> Integer.compare(e2.getId(), e1.getId())) // Sort by ID descending (newest first)
                .limit(5)
                .toList();
        model.addAttribute("recentEvents", recentEvents);
        
        // Add ads data for the carousel
        model.addAttribute("ads", adService.getAllAds());
        
        return "dashboard";
    }
    
}