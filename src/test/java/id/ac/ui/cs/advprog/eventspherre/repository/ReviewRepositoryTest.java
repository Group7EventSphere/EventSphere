package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewRepositoryTest {

    private ReviewRepository repo;

    @BeforeEach
    void setUp() {
        repo = mock(ReviewRepository.class);
    }

    @Test
    void saveAndFind() {
        Review input = new Review(10, 20L, "Test review", 4);
        Review saved = new Review(10, 20L, "Test review", 4);
        saved.setId(1L);

        when(repo.save(input)).thenReturn(saved);
        when(repo.findById(1L)).thenReturn(Optional.of(saved));

        Review result = repo.save(input);
        Optional<Review> found = repo.findById(1L);

        assertNotNull(result.getId(), "ID should be generated");
        assertTrue(found.isPresent(), "Review must be retrievable");
        assertEquals("Test review", found.get().getReviewText());
        verify(repo).save(input);
        verify(repo).findById(1L);
    }

    @Test
    void deleteReview() {
        Long id = 2L;
        doNothing().when(repo).deleteById(id);
        when(repo.findById(id)).thenReturn(Optional.empty());

        repo.deleteById(id);
        Optional<Review> found = repo.findById(id);

        assertFalse(found.isPresent(), "Review should be gone after delete");
        verify(repo).deleteById(id);
        verify(repo).findById(id);
    }

    @Test
    void findByEventId_returnsAllMatchingReviews() {
        Review a = new Review(100, 10L, "First", 5);
        a.setId(3L);
        Review b = new Review(100, 11L, "Second", 4);
        b.setId(4L);

        when(repo.findByEventId(100)).thenReturn(List.of(a, b));

        List<Review> list = repo.findByEventId(100);

        assertEquals(2, list.size(),
                "Should return exactly 2 reviews for event 100");
        assertTrue(list.contains(a));
        assertTrue(list.contains(b));
        verify(repo).findByEventId(100);
    }

    @Test
    void findByAttendeeIdAndEventId_returnsMatchingReview() {
        Review a = new Review(200, 20L, "Mine", 2);
        a.setId(5L);

        when(repo.findByAttendeeIdAndEventId(20L, 200))
                .thenReturn(Optional.of(a));

        Optional<Review> opt = repo.findByAttendeeIdAndEventId(20L, 200);

        assertTrue(opt.isPresent(),
                "Should find the review by attendee 20 on event 200");
        assertEquals(a.getId(), opt.get().getId());
        assertEquals("Mine", opt.get().getReviewText());
        verify(repo).findByAttendeeIdAndEventId(20L, 200);
    }
}
