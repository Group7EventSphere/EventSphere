package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }    @GetMapping
    public String profilePage(Model model, Principal principal) {
        // Get current user
        User user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        
        return AppConstants.VIEW_PROFILE;
    }
    
    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String phoneNumber,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get current user
            User user = userService.getUserByEmail(principal.getName());
            
            // Update user details but not role
            user.setName(name);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
              // Save updated user
            userService.updateUser(user.getId(), name, email, phoneNumber);
            
            redirectAttributes.addFlashAttribute("successMessage", AppConstants.SUCCESS_PROFILE_UPDATED);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", AppConstants.ERROR_UPDATING_PROFILE + e.getMessage());
        }
        
        return AppConstants.REDIRECT_PROFILE;
    }
    
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmNewPassword,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get current user
            User user = userService.getUserByEmail(principal.getName());
              // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", AppConstants.ERROR_CURRENT_PASSWORD_INCORRECT);
                return AppConstants.REDIRECT_PROFILE;
            }
            
            // Verify new password matches confirmation
            if (!newPassword.equals(confirmNewPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", AppConstants.ERROR_NEW_PASSWORDS_NO_MATCH);
                return AppConstants.REDIRECT_PROFILE;
            }
              // Update password
            userService.updateUserPassword(user.getId(), newPassword);
            
            redirectAttributes.addFlashAttribute("successMessage", AppConstants.SUCCESS_PASSWORD_CHANGED);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", AppConstants.ERROR_CHANGING_PASSWORD + e.getMessage());
        }
        
        return AppConstants.REDIRECT_PROFILE;
    }
}