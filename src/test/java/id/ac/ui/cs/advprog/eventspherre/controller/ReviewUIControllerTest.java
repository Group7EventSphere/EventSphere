package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewUIControllerTest {

    private ReviewRepository repo;
    private ReviewUIController controller;

    @BeforeEach
    void setUp() {
        repo = mock(ReviewRepository.class);
        controller = new ReviewUIController(repo);
    }

    @Test
    void reviewsPage_addsAllReviewsToModel_andReturnsViewName() {
        List<Review> reviews = Arrays.asList(
                new Review(1, 10L, "Test1", 4),
                new Review(2, 20L, "Test2", 5)
        );
        when(repo.findAll()).thenReturn(reviews);

        Model model = new ExtendedModelMap();
        String viewName = controller.reviewsPage(model);

        assertEquals("reviews", viewName, "should return the 'reviews' template name");
        assertSame(reviews, model.getAttribute("reviews"),
                "model must contain the list under key 'reviews'");
        verify(repo).findAll();
    }
}
