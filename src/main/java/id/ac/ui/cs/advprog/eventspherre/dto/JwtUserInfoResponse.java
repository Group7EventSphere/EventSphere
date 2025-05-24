package id.ac.ui.cs.advprog.eventspherre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtUserInfoResponse {
    private Integer id;
    private String email;
    private String name;
    private String phoneNumber;
    private String role;
    private Double balance;
}