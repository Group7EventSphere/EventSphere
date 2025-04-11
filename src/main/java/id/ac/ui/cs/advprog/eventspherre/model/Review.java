package id.ac.ui.cs.advprog.eventspherre.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Review {

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
