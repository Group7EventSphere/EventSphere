package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository repo;
      @Autowired
    private UserRepository userRepo;
      private Long user1Id, user2Id, user10Id, user20Id, user100Id, user200Id;
    
    @BeforeEach
    void setUp() {
        // Create test users and get their generated IDs
        user1Id = createUser("user1@test.com", "User One");
        user2Id = createUser("user2@test.com", "User Two");
        user10Id = createUser("user10@test.com", "User Ten");
        user20Id = createUser("user20@test.com", "User Twenty");
        user100Id = createUser("user100@test.com", "User Hundred");
        user200Id = createUser("user200@test.com", "User Two Hundred");
    }
    
    private Long createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword("password");
        user.setRole(User.Role.ATTENDEE);
        User savedUser = userRepo.save(user);
        return savedUser.getId().longValue(); // Convert Integer to Long
    }    @Test
    void saveAndFind() {
        Review input = new Review(20, user10Id, "Test review", 4);

        Review saved = repo.save(input);

        assertNotNull(saved.getId(), "ID should be generated");
        Optional<Review> found = repo.findById(saved.getId());
        assertTrue(found.isPresent(), "Review must be retrievable");
        assertEquals("Test review", found.get().getReviewText());
    }

    @Test
    void deleteReview() {
        Review toDelete = repo.save(new Review(2, user1Id, "To delete", 3));
        Long id = toDelete.getId();

        repo.deleteById(id);

        Optional<Review> found = repo.findById(id);
        assertFalse(found.isPresent(), "Review should be gone after delete");
    }

    @Test
    void findByEventId_returnsAllMatchingReviews() {
        Review a = repo.save(new Review(100, user100Id, "First", 5));
        Review b = repo.save(new Review(100, user200Id, "Second", 4));
        repo.save(new Review(101, user100Id, "Other event", 3));

        List<Review> list = repo.findByEventId(100);

        assertEquals(2, list.size(), "Should return exactly 2 reviews for event 100");
        assertTrue(list.stream().anyMatch(r -> r.getId().equals(a.getId())));
        assertTrue(list.stream().anyMatch(r -> r.getId().equals(b.getId())));
    }    @Test
    void findByAttendeeIdAndEventId_returnsMatchingReview() {
        Review a = repo.save(new Review(200, user20Id, "Mine", 2));
        repo.save(new Review(200, user10Id, "Not mine", 3));

        Optional<Review> opt = repo.findByAttendeeIdAndEventId(user20Id, 200);

        assertTrue(opt.isPresent(), "Should find the review by attendee " + user20Id + " on event 200");
        assertEquals(a.getId(), opt.get().getId());
        assertEquals("Mine", opt.get().getReviewText());
    }
}
