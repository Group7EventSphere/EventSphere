package id.ac.ui.cs.advprog.eventspherre.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "unauthorized";
    }
    
    @GetMapping("/error")
    public String handleError() {
        // You can add error handling logic here if needed
        return "error";
    }
}