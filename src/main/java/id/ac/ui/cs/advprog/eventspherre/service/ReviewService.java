package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import java.util.Optional;

public interface ReviewService {
    Review create(Review review);
    Optional<Review> findById(Long id);
    Review update(Long id, Review review);
    boolean delete(Long id);
}
