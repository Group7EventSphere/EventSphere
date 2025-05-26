package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.repository.ReviewRepository;
import id.ac.ui.cs.advprog.eventspherre.validation.ReviewValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {    private static final String INVALID_MSG = AppConstants.ERROR_INVALID_REVIEW;
    private static final String NOT_FOUND_MSG = AppConstants.ERROR_REVIEW_NOT_FOUND;

    private final ReviewRepository repo;
    private final ReviewValidator validator;

    public ReviewServiceImpl(ReviewRepository repo, ReviewValidator validator) {
        this.repo      = repo;
        this.validator = validator;
    }

    @Override
    public Review create(Review review) {
        if (!validator.isValid(review)) {
            throw new IllegalArgumentException(INVALID_MSG);
        }

        Optional<Review> existing =
                repo.findByAttendeeIdAndEventId(review.getAttendeeId(), review.getEventId());        if (existing.isPresent()) {
            throw new IllegalStateException(AppConstants.ERROR_ALREADY_SUBMITTED_REVIEW);
        }

        return repo.save(review);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Review update(Long id, Review newData) {
        Review existing = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException(NOT_FOUND_MSG));

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
    public List<Review> findByEventId(int eventId) {
        return repo.findByEventId(eventId);
    }
}