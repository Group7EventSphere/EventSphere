package id.ac.ui.cs.advprog.eventspherre.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtRegisterRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = AppConstants.MIN_PASSWORD_LENGTH, message = "Password must be at least 6 characters")
    private String password;
    
    private String phoneNumber;
    
    private String role;
}