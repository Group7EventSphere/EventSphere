package id.ac.ui.cs.advprog.eventspherre.validation;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import org.springframework.stereotype.Component;

@Component
public class DefaultReviewValidator implements ReviewValidator {
    @Override
    public boolean isValid(Review review) {
        return review.getReviewText() != null
                && !review.getReviewText().trim().isEmpty()
                && review.getRating() >= AppConstants.MIN_RATING
                && review.getRating() <= AppConstants.MAX_RATING;
    }

    @Override
    public String getErrorMessage() {
        return AppConstants.ERROR_INVALID_REVIEW;
    }
}
