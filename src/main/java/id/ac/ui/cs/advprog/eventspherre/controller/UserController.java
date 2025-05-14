package id.ac.ui.cs.advprog.eventspherre.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class MainController {
    
    @GetMapping("/")
    public String Login() {
        // The string returned here is the logical name of the template view
        return "LoginPage";
    }

    @GetMapping("/me")
    public String AuthenticatedUser() {
        // The string returned here is the logical name of the template view
        return "MePage";
    }
    
}
