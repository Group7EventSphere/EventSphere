package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.dto.RegisterUserDto;
import id.ac.ui.cs.advprog.eventspherre.service.AuthenticationService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/")
public class AuthenticationController {
    private final AuthenticationService authService;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new RegisterUserDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterUserDto dto) {
        authService.signup(dto);
        return "redirect:/login";
    }
}
