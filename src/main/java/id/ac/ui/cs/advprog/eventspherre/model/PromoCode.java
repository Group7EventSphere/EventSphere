package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
@Entity
@Table(name = "promo_codes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal discountPercentage;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate validFrom;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate validUntil;

    @Column(nullable = false)
    private Integer maxUsage;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentUsage = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;


    @Column(name = "organizer_id", nullable = false)
    private Integer organizerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return isActive && 
               !today.isBefore(validFrom) && 
               !today.isAfter(validUntil) && 
               currentUsage < maxUsage;
    }

    public void incrementUsage() {
        this.currentUsage++;
    }
}