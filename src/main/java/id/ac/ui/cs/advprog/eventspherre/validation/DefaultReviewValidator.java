package id.ac.ui.cs.advprog.eventspherre.validation;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import org.springframework.stereotype.Component;

@Component
public class DefaultReviewValidator implements ReviewValidator {
    @Override
    public boolean isValid(Review review) {
        if (review.getReviewText() == null || review.getReviewText().trim().isEmpty()) {
            return false;
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            return false;
        }
        return true;
    }

    @Override
    public String getErrorMessage() {
        return "Invalid review: Review text cannot be empty and rating must be between 1 and 5.";
    }
}
