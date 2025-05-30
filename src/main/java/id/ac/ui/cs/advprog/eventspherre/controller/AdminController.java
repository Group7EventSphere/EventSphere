package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import id.ac.ui.cs.advprog.eventspherre.dto.RegisterUserDto;
import id.ac.ui.cs.advprog.eventspherre.service.AuthenticationService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import id.ac.ui.cs.advprog.eventspherre.model.User;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Arrays;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
      @GetMapping
    public String redirectToUserManagement() {
        return AppConstants.REDIRECT_ADMIN_USERS;
    }

    private final UserService userService;
    private final AuthenticationService authenticationService;

    public AdminController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/users")
    public String userManagement(
            Model model, 
            Principal principal,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search) {
        
        List<User> users;
        
        // Handle the search and filter logic
        if (role != null && !role.isEmpty()) {
            // If both role filter and search term are provided
            if (search != null && !search.isEmpty()) {
                try {                    users = userService.searchUsersByRoleAndTerm(role, search);                } catch (IllegalArgumentException e) {
                    // If invalid role is provided, fallback to just search
                    users = userService.searchUsers(search);
                    model.addAttribute(AppConstants.ATTR_ERROR_MESSAGE, AppConstants.ERROR_INVALID_ROLE_FILTER);
                }
            } else {
                // If only role filter is provided
                try {
                    users = userService.getUsersByRole(role);                } catch (IllegalArgumentException e) {
                    // If invalid role is provided, fallback to all users
                    users = userService.getAllUsers();
                    model.addAttribute(AppConstants.ATTR_ERROR_MESSAGE, AppConstants.ERROR_INVALID_ROLE_ALL_USERS);
                }
            }
        } else if (search != null && !search.isEmpty()) {
            // If only search term is provided
            users = userService.searchUsers(search);
        } else {
            // If no filters are provided
            users = userService.getAllUsers();
        }
        
        // Add the users to the model
        model.addAttribute("users", users);
        
        // Add the current user's email for comparison in the view
        if (principal != null) {
            model.addAttribute("currentUserEmail", principal.getName());
        }
        
        // Add the current filter and search values for preserving the state
        model.addAttribute("currentRole", role);
        model.addAttribute("currentSearch", search);
          // Add available roles for the dropdown
        model.addAttribute("availableRoles", Arrays.asList(User.Role.values()));
        
        return AppConstants.VIEW_ADMIN_USER_MANAGEMENT;
    }    @PostMapping("/users/{id}/update")
    public String updateUser(
            @PathVariable Integer id, 
            @RequestParam String name, 
            @RequestParam String email, 
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String role,
            Principal principal,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String currentRole,
            @RequestParam(required = false) String currentSearch) {
        try {
            // Get the current user's email
            String currentUserEmail = principal.getName();
            
            // Update user details
            userService.updateUser(id, name, email, phoneNumber);
            
            // Update role if provided and meets restrictions
            if (role != null && !role.isEmpty()) {
                User user = userService.getUserById(id);
                
                // Check if user is trying to change their own role (admin restriction)
                boolean isCurrentUser = user.getEmail().equals(currentUserEmail);
                
                if (isCurrentUser && user.getRole() == User.Role.ADMIN) {
                    // Skip role update for admin's own account
                    redirectAttributes.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE, AppConstants.SUCCESS_USER_UPDATED_NO_ROLE);
                } else {
                    userService.updateUserRole(id, role);
                    redirectAttributes.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE, AppConstants.SUCCESS_USER_UPDATED);
                }
            } else {
                redirectAttributes.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE, AppConstants.SUCCESS_USER_UPDATED);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE, AppConstants.ERROR_UPDATING_USER + e.getMessage());
        }
        
        // Preserve the filter and search state
        if (currentRole != null && !currentRole.isEmpty()) {
            redirectAttributes.addAttribute("role", currentRole);
        }
        if (currentSearch != null && !currentSearch.isEmpty()) {
            redirectAttributes.addAttribute("search", currentSearch);
        }
        
        return AppConstants.REDIRECT_ADMIN_USERS;
    }
    
    @PostMapping("/users/{id}/update-password")
    public String updateUserPassword(
            @PathVariable Integer id, 
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String currentRole,
            @RequestParam(required = false) String currentSearch) {        try {
            userService.updateUserPassword(id, newPassword);            redirectAttributes.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE, AppConstants.SUCCESS_PASSWORD_UPDATED);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE, AppConstants.ERROR_UPDATING_PASSWORD + e.getMessage());
        }
        
        // Preserve the filter and search state
        if (currentRole != null && !currentRole.isEmpty()) {
            redirectAttributes.addAttribute("role", currentRole);
        }
        if (currentSearch != null && !currentSearch.isEmpty()) {            redirectAttributes.addAttribute("search", currentSearch);
        }
        
        return AppConstants.REDIRECT_ADMIN_USERS;
    }
    
    @PostMapping("/users/create")
    public String createUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam String role,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String currentRole,
            @RequestParam(required = false) String currentSearch) {
        try {
            // Create a RegisterUserDto object from the form data
            RegisterUserDto registerUserDto = new RegisterUserDto();
            registerUserDto.setName(name);
            registerUserDto.setEmail(email);
            registerUserDto.setPassword(password);
            registerUserDto.setPhoneNumber(phoneNumber);
            
            // Register the user
            User newUser = authenticationService.signup(registerUserDto);
            
            // Set the user role
            if (role.equals("ADMIN") || role.equals("ORGANIZER") || role.equals("ATTENDEE")) {
                userService.updateUserRole(newUser.getId(), role);            }            
            redirectAttributes.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE, AppConstants.SUCCESS_USER_CREATED);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE, AppConstants.ERROR_CREATING_USER + e.getMessage());
        }
        
        // Preserve the filter and search state
        if (currentRole != null && !currentRole.isEmpty()) {
            redirectAttributes.addAttribute("role", currentRole);
        }
        if (currentSearch != null && !currentSearch.isEmpty()) {            redirectAttributes.addAttribute("search", currentSearch);
        }
        
        return AppConstants.REDIRECT_ADMIN_USERS;
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable Integer id, 
            Principal principal,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String currentRole,
            @RequestParam(required = false) String currentSearch) {
        // Get the user to check if it's the current user
        User user = userService.getUserById(id);        // Prevent deletion of the current user
        if (principal != null && principal.getName().equals(user.getEmail())) {
            redirectAttributes.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE, AppConstants.ERROR_CANNOT_DELETE_OWN_ACCOUNT);
            // Preserve the filter and search state
            if (currentRole != null && !currentRole.isEmpty()) {
                redirectAttributes.addAttribute("role", currentRole);
            }
            if (currentSearch != null && !currentSearch.isEmpty()) {
                redirectAttributes.addAttribute("search", currentSearch);
            }
            return AppConstants.REDIRECT_ADMIN_USERS;
        }
          try {
            userService.deleteUser(id);            redirectAttributes.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE, AppConstants.SUCCESS_USER_DELETED);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE, AppConstants.ERROR_DELETING_USER + e.getMessage());
        }
        
        // Preserve the filter and search state
        if (currentRole != null && !currentRole.isEmpty()) {
            redirectAttributes.addAttribute("role", currentRole);
        }
        if (currentSearch != null && !currentSearch.isEmpty()) {            redirectAttributes.addAttribute("search", currentSearch);
        }
        
        return AppConstants.REDIRECT_ADMIN_USERS;
    }
}