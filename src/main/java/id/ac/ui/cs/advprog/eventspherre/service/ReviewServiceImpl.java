package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.repository.ReviewRepository;
import id.ac.ui.cs.advprog.eventspherre.validation.ReviewValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final String INVALID_MSG = "Invalid review: Review text cannot be empty and rating must be between 1 and 5.";
    private static final String NOT_FOUND_MSG = "Review not found";

    private final ReviewRepository repo;
    private final ReviewValidator validator;

    public ReviewServiceImpl(ReviewRepository repo, ReviewValidator validator) {
        this.repo = repo;
        this.validator = validator;
    }

    @Override
    public Review create(Review review) {
        if (!validator.isValid(review)) {
            throw new IllegalArgumentException(INVALID_MSG);
        }
        return repo.save(review);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Review update(Long id, Review newData) {
        Review existing = repo.findById(id).orElseThrow(() -> new NoSuchElementException(NOT_FOUND_MSG));
        if (!validator.isValid(newData)) {
            throw new IllegalArgumentException(INVALID_MSG);
        }
        existing.setReviewText(newData.getReviewText());
        existing.setRating(newData.getRating());
        return repo.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Review> getReviewsByEventId(UUID eventId) {
        return List.of();
    }

    @Override
    public List<Review> findByEventId(int eventId) {
        return List.of();
    }

    public List<Review> getReviewsByEventId(int eventId) {
        // Convert UUID to String to match with our eventUuid field in the database
        return repo.findByEventId(eventId);
    }

}
