package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AdRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdRepository adRepository;

    private Ad ad;

    @BeforeEach
    void setUp() {
        ad = new Ad(1L, "Test Ad", "Test Description", "test.jpg");
    }

    @AfterEach
    void tearDown() {
        entityManager.clear();
    }

    @Test
    void testSaveAd() {
        Ad savedAd = adRepository.save(ad);
        assertNotNull(savedAd.getId());
        assertEquals("Test Ad", savedAd.getTitle());
        assertEquals("Test Description", savedAd.getDescription());
        assertEquals("test.jpg", savedAd.getImageUrl());
    }

    @Test
    void testFindById() {
        Ad savedAd = entityManager.persist(ad);
        Optional<Ad> foundAd = adRepository.findById(savedAd.getId());

        assertTrue(foundAd.isPresent());
        assertEquals(savedAd.getId(), foundAd.get().getId());
        assertEquals("Test Ad", foundAd.get().getTitle());
    }

    @Test
    void testFindById_NotFound() {
        Optional<Ad> foundAd = adRepository.findById(999L);
        assertFalse(foundAd.isPresent());
    }

    @Test
    void testDeleteAd() {
        Ad savedAd = entityManager.persist(ad);
        adRepository.delete(savedAd);

        Optional<Ad> deletedAd = adRepository.findById(savedAd.getId());
        assertFalse(deletedAd.isPresent());
    }

    @Test
    void testFindAllAds() {
        entityManager.persist(ad);
        entityManager.persist(new Ad(2L, "Second Ad", "Desc", "img2.jpg"));

        Iterable<Ad> ads = adRepository.findAll();
        long count = ads.spliterator().getExactSizeIfKnown();
        assertEquals(2, count);
    }

    @Test
    void testUpdateAd() {
        Ad savedAd = entityManager.persist(ad);
        Ad updatedAd = new Ad(savedAd.getId(), "Updated Title", "Updated Description",
                savedAd.getImageUrl());

        Ad result = adRepository.save(updatedAd);

        assertEquals(savedAd.getId(), result.getId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
    }
}