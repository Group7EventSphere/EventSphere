package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="reviews")
@Getter
@Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long eventId;
    private Long attendeeId;
    private String reviewText;
    private int rating;

    public Review() { }

    public Review(Long eventId, Long attendeeId, String reviewText, int rating) {
        this.eventId = eventId;
        this.attendeeId = attendeeId;
        this.reviewText = reviewText;
        this.rating = rating;
    }
}
