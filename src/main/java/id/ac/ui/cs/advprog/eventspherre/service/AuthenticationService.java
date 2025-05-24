package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.dto.LoginUserDto;
import id.ac.ui.cs.advprog.eventspherre.dto.RegisterUserDto;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUserDto input) {
        User user = new User();
        user.setEmail(input.getEmail());
        user.setName(input.getName());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setPhoneNumber(input.getPhoneNumber());
        
        // For testing: If email contains "admin", set role to ADMIN
        if (input.getEmail() != null && input.getEmail().contains("admin")) {
            user.setRole(User.Role.ADMIN);
        } else if (input.getEmail() != null && input.getEmail().contains("organizer")) {
            user.setRole(User.Role.ORGANIZER);
        } else {
            user.setRole(User.Role.ATTENDEE);
        }

        return userRepository.save(user);
    }
}
