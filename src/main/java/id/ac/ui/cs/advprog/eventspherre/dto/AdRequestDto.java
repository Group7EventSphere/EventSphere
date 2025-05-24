package id.ac.ui.cs.advprog.eventspherre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdRequestDto {

    private String title;
    private String description;
    private String imageUrl;
}
