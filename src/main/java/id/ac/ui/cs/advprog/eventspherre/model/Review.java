package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="reviews")
@Getter
@Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private int eventId; // Changed from Long to int

    private Long attendeeId;
    private String reviewText;
    private int rating;

    public Review() { }

    // Constructor updated to use int eventId
    public Review(int eventId, Long attendeeId, String reviewText, int rating) {
        this.eventId = eventId;
        this.attendeeId = attendeeId;
        this.reviewText = reviewText;
        this.rating = rating;
    }
}

