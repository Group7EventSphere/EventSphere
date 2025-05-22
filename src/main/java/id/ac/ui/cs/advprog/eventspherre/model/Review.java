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
    private Long eventId;

    @Column(name = "event_uuid")
    private String eventUuid;


    private Long attendeeId;
    private String reviewText;
    private int rating;

    public Review() { }


    public Review(UUID eventUuid, Long attendeeId, String reviewText, int rating) {
        this.eventUuid = eventUuid.toString();
        this.attendeeId = attendeeId;
        this.reviewText = reviewText;
        this.rating = rating;
    }

    // For backward compatibility with existing data
    public Review(Long eventId, Long attendeeId, String reviewText, int rating) {

        this.eventId = eventId;
        this.attendeeId = attendeeId;
        this.reviewText = reviewText;
        this.rating = rating;
    }
}

