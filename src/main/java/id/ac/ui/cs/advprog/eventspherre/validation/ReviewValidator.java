package id.ac.ui.cs.advprog.eventspherre.validation;

import id.ac.ui.cs.advprog.eventspherre.model.Review;

public interface ReviewValidator {

    boolean isValid(Review review);

    String getErrorMessage();
}
