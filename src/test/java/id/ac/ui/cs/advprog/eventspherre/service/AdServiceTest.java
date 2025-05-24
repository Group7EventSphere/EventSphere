package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.repository.AdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdServiceTest {

    @Mock
    private AdRepository adRepository;

    @InjectMocks
    private AdService adService;

    private Ad ad;

    @BeforeEach
    void setUp() {
        // Initialize a sample ad
        ad = new Ad(1L, "Test Ad", "This is a test ad description", "image1.jpg", "ADMIN");
    }

    @Test
    void testCreateAd() {
        when(adRepository.save(any(Ad.class))).thenReturn(ad);

        Ad createdAd = adService.createAd(ad);
        assertEquals("Test Ad", createdAd.getTitle(), "Should be able to create an ad");
        verify(adRepository, times(1)).save(ad);
    }

    @Test
    void testGetAd() {
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));

        Ad foundAd = adService.getAd(1L);
        assertNotNull(foundAd, "Should be able to get an ad by ID");
        assertEquals("Test Ad", foundAd.getTitle(), "Retrieved ad should match");
    }

    @Test
    void testGetAdNotFound() {
        when(adRepository.findById(1L)).thenReturn(Optional.empty());

        Ad foundAd = adService.getAd(1L);
        assertNull(foundAd, "Should return null when ad not found");
    }

    @Test
    void testUpdateAd() {
        Ad updatedAd = new Ad(1L, "Updated Test Ad", "Updated Description", "updated_image.jpg", "ADMIN");
        when(adRepository.existsById(1L)).thenReturn(true);
        when(adRepository.save(any(Ad.class))).thenReturn(updatedAd);

        Ad result = adService.updateAd(1L, updatedAd);
        assertEquals("Updated Test Ad", result.getTitle(), "Should be able to update an ad");
    }

    @Test
    void testUpdateAdNotFound() {
        Ad updatedAd = new Ad(1L, "Updated Test Ad", "Updated Description", "updated_image.jpg", "ADMIN");
        when(adRepository.existsById(1L)).thenReturn(false);

        Ad result = adService.updateAd(1L, updatedAd);
        assertNull(result, "Should return null when ad to update doesn't exist");
    }

    @Test
    void testDeleteAd() {
        doNothing().when(adRepository).deleteById(1L);

        adService.deleteAd(1L);
        verify(adRepository, times(1)).deleteById(1L);
    }
}