package id.ac.ui.cs.advprog.eventspherre.controller.api;

import id.ac.ui.cs.advprog.eventspherre.dto.*;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.AuthenticationService;
import id.ac.ui.cs.advprog.eventspherre.service.JwtService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Profile("!test")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody JwtAuthRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Load user details and generate JWT tokens
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Find the user entity
            User user = userService.findByEmail(loginRequest.getEmail());

            // Return response with tokens
            return ResponseEntity.ok(JwtAuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hour
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .build());
                    
        } catch (BadCredentialsException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unauthorized");
            response.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody JwtRegisterRequest registerRequest) {
        try {
            // Check if email already exists
            if (userService.existsByEmail(registerRequest.getEmail())) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Bad Request");
                response.put("message", "Email is already in use");
                return ResponseEntity.badRequest().body(response);
            }

            // Create RegisterUserDto from JwtRegisterRequest
            RegisterUserDto registerUserDto = new RegisterUserDto();
            registerUserDto.setName(registerRequest.getName());
            registerUserDto.setEmail(registerRequest.getEmail());
            registerUserDto.setPassword(registerRequest.getPassword());
            registerUserDto.setPhoneNumber(registerRequest.getPhoneNumber());

            // Register user
            User user = authenticationService.signup(registerUserDto);

            // Update role if specified and valid
            if (registerRequest.getRole() != null && !registerRequest.getRole().isEmpty()) {
                userService.updateUserRole(user.getId(), registerRequest.getRole());
                // Reload the user to get the updated role
                user = userService.getUserById(user.getId());
            }

            // Generate JWT tokens
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Return response with tokens
            return ResponseEntity.status(HttpStatus.CREATED).body(JwtAuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hour
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .build());
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Bad Request");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody JwtRefreshRequest refreshRequest) {
        try {
            // Extract email from refresh token
            String email = jwtService.extractUsername(refreshRequest.getRefreshToken());

            // Check if the refresh token is valid
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(refreshRequest.getRefreshToken(), userDetails)) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Unauthorized");
                response.put("message", "Invalid refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Generate new access token
            String accessToken = jwtService.generateToken(userDetails);

            // Find the user entity
            User user = userService.findByEmail(email);

            // Return response with new access token
            return ResponseEntity.ok(JwtAuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshRequest.getRefreshToken()) // Return the same refresh token
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hour
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .build());
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unauthorized");
            response.put("message", "Invalid refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            // Get the currently authenticated user's email
            String email = authentication.getName();
            
            // Find the user entity
            User user = userService.findByEmail(email);
            
            // Return user info
            return ResponseEntity.ok(JwtUserInfoResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .phoneNumber(user.getPhoneNumber())
                    .role(user.getRole().name())
                    .balance(user.getBalance())
                    .build());
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unauthorized");
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}