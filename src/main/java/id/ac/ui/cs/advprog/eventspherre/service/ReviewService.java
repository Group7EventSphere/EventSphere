package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewService {
    Review create(Review review);
    Optional<Review> findById(Long id);
    Review update(Long id, Review review);
    boolean delete(Long id);

    // Added method for integration with Event feature
    List<Review> getReviewsByEventId(UUID eventId);

    List<Review> findByEventId(int eventId);
}

