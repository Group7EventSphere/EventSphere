package id.ac.ui.cs.advprog.eventspherre.dto;

import lombok.Data;
import lombok.Builder;

@Builder
@Data
public class AdResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
}