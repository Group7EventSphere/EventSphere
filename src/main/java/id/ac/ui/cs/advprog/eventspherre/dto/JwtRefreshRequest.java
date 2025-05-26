package id.ac.ui.cs.advprog.eventspherre.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtRefreshRequest {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}