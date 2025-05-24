package id.ac.ui.cs.advprog.eventspherre.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "unauthorized";
    }
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = "An unexpected error occurred";
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                errorMessage = "Page not found";
                model.addAttribute("errorCode", "404");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                errorMessage = "Access denied";
                model.addAttribute("errorCode", "403");
                return "unauthorized";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                errorMessage = "Internal server error";
                model.addAttribute("errorCode", "500");
            }
        }
        
        model.addAttribute("errorMessage", errorMessage);
        return "error";
    }
}