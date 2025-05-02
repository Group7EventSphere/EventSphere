package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.repository.ReviewRepository;
import id.ac.ui.cs.advprog.eventspherre.validation.ReviewValidator;

import java.util.Optional;

public class ReviewServiceImpl implements ReviewService {

    private static final String INVALID_MSG =
            "Invalid review: Review text cannot be empty and rating must be between 1 and 5.";

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
}