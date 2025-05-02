package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AdRepositoryTest {

    @Autowired
    private AdRepository adRepository;

    private Ad ad;

    @BeforeEach
    void setUp() {
        ad = new Ad(1L, "Test Ad", "Description", "image.jpg", "ADMIN", true);
    }

    @Test
    void testSaveAd() {
        Ad savedAd = adRepository.save(ad);
        assertNotNull(savedAd.getId());
        assertEquals("Test Ad", savedAd.getTitle());
        assertEquals("Description", savedAd.getDescription());
        assertEquals("image.jpg", savedAd.getImageUrl());
    }

    @Test
    void testFindById() {
        adRepository.save(ad);
        Ad foundAd = adRepository.findById(ad.getId()).orElse(null);
        assertNotNull(foundAd);
        assertEquals(ad.getId(), foundAd.getId());
    }

    @Test
    void testDeleteAd() {
        adRepository.save(ad);
        adRepository.delete(ad);
        Ad foundAd = adRepository.findById(ad.getId()).orElse(null);
        assertNull(foundAd);
    }

    @Test
    void testFindAllAds() {
        adRepository.save(ad);
        Iterable<Ad> ads = adRepository.findAll();
        assertTrue(ads.iterator().hasNext());
    }
}