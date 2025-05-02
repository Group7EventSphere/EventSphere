package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdRepository adRepository;

    private Ad ad;

    @BeforeEach
    void setUp() {
        // Initialize with test data
        ad = new Ad();
        ad.setTitle("Test Ad");
        ad.setDescription("Test Description");
        ad.setImageUrl("test.jpg");
        // Clear any existing data
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        entityManager.clear();
    }

    @Test
    void testSaveAd() {
        // Act
        Ad savedAd = adRepository.save(ad);

        // Assert
        assertNotNull(savedAd.getId(), "Saved ad should have an ID");
        assertEquals("Test Ad", savedAd.getTitle(), "Titles should match");
        assertEquals("Test Description", savedAd.getDescription(), "Descriptions should match");
        assertEquals("test.jpg", savedAd.getImageUrl(), "Image URLs should match");
    }

    @Test
    void testFindById() {
        // Arrange
        Ad savedAd = entityManager.persist(ad);

        // Act
        Optional<Ad> foundAd = adRepository.findById(savedAd.getId());

        // Assert
        assertTrue(foundAd.isPresent(), "Ad should be found");
        assertEquals(savedAd.getId(), foundAd.get().getId(), "IDs should match");
        assertEquals("Test Ad", foundAd.get().getTitle(), "Titles should match");
    }

    @Test
    void testFindById_NotFound() {
        // Act
        Optional<Ad> foundAd = adRepository.findById(999L);

        // Assert
        assertFalse(foundAd.isPresent(), "Ad should not be found");
    }

    @Test
    void testDeleteAd() {
        // Arrange
        Ad savedAd = entityManager.persist(ad);

        // Act
        adRepository.delete(savedAd);

        // Assert
        Optional<Ad> deletedAd = adRepository.findById(savedAd.getId());
        assertFalse(deletedAd.isPresent(), "Ad should be deleted");
    }

    @Test
    void testFindAllAds() {
        // Arrange
        entityManager.persist(ad);

        Ad secondAd = new Ad();
        secondAd.setTitle("Second Ad");
        secondAd.setDescription("Desc");
        secondAd.setImageUrl("img2.jpg");
        entityManager.persist(secondAd);

        // Act
        Iterable<Ad> ads = adRepository.findAll();

        // Assert
        List<Ad> adList = (List<Ad>) ads;
        assertEquals(2, adList.size(), "Should find 2 ads");
        assertTrue(adList.stream().anyMatch(a -> a.getTitle().equals("Test Ad")),
                "Should contain first ad");
        assertTrue(adList.stream().anyMatch(a -> a.getTitle().equals("Second Ad")),
                "Should contain second ad");
    }

    @Test
    void testUpdateAd() {
        // Arrange
        Ad savedAd = entityManager.persist(ad);

        Ad updatedAd = new Ad();
        updatedAd.setId(savedAd.getId());
        updatedAd.setTitle("Updated Title");
        updatedAd.setDescription("Updated Description");
        updatedAd.setImageUrl("updated.jpg");

        // Act
        Ad result = adRepository.save(updatedAd);

        // Assert
        assertEquals(savedAd.getId(), result.getId(), "IDs should match");
        assertEquals("Updated Title", result.getTitle(), "Title should be updated");
        assertEquals("Updated Description", result.getDescription(), "Description should be updated");
        assertEquals("updated.jpg", result.getImageUrl(), "Image URL should be updated");

        // Verify the update persisted
        Optional<Ad> verifiedAd = adRepository.findById(savedAd.getId());
        assertTrue(verifiedAd.isPresent(), "Ad should exist");
        assertEquals("Updated Title", verifiedAd.get().getTitle(), "Title should be updated in DB");
    }

    @Test
    void testCountAds() {
        // Arrange
        entityManager.persist(ad);

        Ad secondAd = new Ad();
        secondAd.setTitle("Second Ad");
        secondAd.setDescription("Desc");
        secondAd.setImageUrl("img2.jpg");
        entityManager.persist(secondAd);

        // Act
        long count = adRepository.count();

        // Assert
        assertEquals(2, count, "Should count 2 ads");
    }
}