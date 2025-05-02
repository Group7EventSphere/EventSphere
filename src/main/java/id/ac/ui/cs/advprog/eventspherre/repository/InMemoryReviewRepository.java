package id.ac.ui.cs.advprog.eventspherre.repository;

import org.springframework.stereotype.Repository;
import id.ac.ui.cs.advprog.eventspherre.model.Review;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryReviewRepository implements ReviewRepository {
    private final Map<Long, Review> store = new HashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Review save(Review review) {
        if (review.getId() == null) {
            review.setId(idSequence.getAndIncrement());
        }
        store.put(review.getId(), review);
        return review;
    }

    @Override
    public Optional<Review> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}