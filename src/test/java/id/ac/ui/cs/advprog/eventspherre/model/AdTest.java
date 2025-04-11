package id.ac.ui.cs.advprog.eventspherre.model;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

public class AdTest {

    @Mock
    private AdService adService;

    private Ad ad;

    @Test
    public void testCreateAdWithInvalidImage() {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
        assertThrows(IOException.class, () -> {
            adService.createAd(ad, invalidFile);
        });
    }

    @Test
    public void testCreateAdWithMissingTitle() {
        ad.setDescription("This is a test ad description.");
        assertThrows(IllegalArgumentException.class, () -> {
            adService.createAd(ad, new MockMultipartFile("file", "test.jpg", "image/jpeg", "image data".getBytes())); // Missing title
        });
    }

    @Test
    public void testCreateAdWithMissingDescription() {
        ad.setTitle("Test Ad");
        assertThrows(IllegalArgumentException.class, () -> {
            adService.createAd(ad, new MockMultipartFile("file", "test.jpg", "image/jpeg", "image data".getBytes())); // Missing description
        });
    }

    @Test
    public void testDeleteNonExistentAd() {
        Long nonExistentAdId = 999L;
        assertThrows(RuntimeException.class, () -> {
            adService.deleteAd(nonExistentAdId); // This should fail since the ad doesn't exist
        });
    }
}