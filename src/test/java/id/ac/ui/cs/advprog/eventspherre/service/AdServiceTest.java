package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.model.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.eventspherre.repository.AdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AdServiceTest {

    @Mock
    private AdRepository adRepository;

    @InjectMocks
    private AdService adService;

    private Ad ad;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Using the correct constructor
        ad = new Ad(1L, "Test Ad", "Description", "image.jpg", "ADMIN", true); // Use constructor with userRole and isActive
    }

    // Test create ad as admin
    @Test
    void testCreateAdAsAdmin() {
        when(adRepository.save(ad)).thenReturn(ad);

        Ad result = adService.createAd(ad);

        assertEquals(ad, result);
        verify(adRepository, times(1)).save(ad); // Ensure save was called once
    }

    // Test create ad as non-admin user
    @Test
    void testUnauthorizedCreateAdAsUser() {
        Ad userAd = new Ad(2L, "Test Ad", "Description", "image.jpg", "USER", true);

        assertThrows(UnauthorizedAccessException.class, () -> adService.createAd(userAd),
                "Only admins can create ads.");
    }

    // Test update ad as admin
    @Test
    void testUpdateAdAsAdmin() {
        Ad updatedAd = new Ad(1L, "Updated Test Ad", "Updated Description", "updated_image.jpg", "ADMIN", true);
        when(adRepository.findById(1L)).thenReturn(java.util.Optional.of(ad));
        when(adRepository.save(updatedAd)).thenReturn(updatedAd);

        Ad result = adService.updateAd(1L, updatedAd);

        assertEquals(updatedAd, result);
        verify(adRepository, times(1)).save(updatedAd); // Ensure save was called once
    }

    // Test update ad as non-admin user
    @Test
    void testUnauthorizedUpdateAdAsUser() {
        Ad updatedAd = new Ad(1L, "Updated Test Ad", "Updated Description", "updated_image.jpg", "USER", true);

        assertThrows(UnauthorizedAccessException.class, () -> adService.updateAd(1L, updatedAd),
                "Only admins can edit ads.");
    }
}
