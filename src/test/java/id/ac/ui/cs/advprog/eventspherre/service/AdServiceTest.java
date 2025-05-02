package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdServiceTest {

    private AdService adServiceAdmin;
    private AdService adServiceUser;
    private Ad ad;

    @BeforeEach
    void setUp() {
        adServiceAdmin = new AdService("ADMIN");  // Admin user
        adServiceUser = new AdService("USER");    // Regular user

        // Initialize a sample ad
        ad = new Ad(1L, "Test Ad", "This is a test ad description", "image1.jpg", "ADMIN");
    }

    @Test
    void testAdminCanCreateAd() {
        Ad createdAd = adServiceAdmin.createAd(ad);
        assertEquals("Test Ad", createdAd.getTitle(), "Admin should be able to create an ad");
    }

    @Test
    void testNonAdminCannotCreateAd() {
        assertThrows(UnauthorizedAccessException.class, () -> adServiceUser.createAd(ad),
                "Non-admin should not be able to create an ad");
    }

    @Test
    void testAdminCanUpdateAd() {
        Ad updatedAd = new Ad(1L, "Updated Test Ad", "Updated Description", "updated_image.jpg", "ADMIN");
        Ad result = adServiceAdmin.updateAd(1L, updatedAd);
        assertEquals("Updated Test Ad", result.getTitle(), "Admin should be able to update an ad");
    }

    @Test
    void testNonAdminCannotUpdateAd() {
        Ad updatedAd = new Ad(1L, "Updated Test Ad", "Updated Description", "updated_image.jpg", "USER");
        assertThrows(UnauthorizedAccessException.class, () -> adServiceUser.updateAd(1L, updatedAd),
                "Non-admin should not be able to update an ad");
    }

    @Test
    void testAnyUserCanViewAd() {
        Ad viewedAd = adServiceUser.viewAd(1L);
        assertNotNull(viewedAd, "Any user should be able to view an ad");
    }
}