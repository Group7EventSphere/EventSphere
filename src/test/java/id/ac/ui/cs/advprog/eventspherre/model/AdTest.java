package id.ac.ui.cs.advprog.eventspherre.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdTest {

    private Ad ad;

    @BeforeEach
    void setUp() {
        ad = new Ad(1L, "Test Ad", "This is a test ad description", "image1.jpg");
    }

    @Test
    void testAdCreation() {
        assertNotNull(ad);
        assertEquals(1L, ad.getId());
        assertEquals("Test Ad", ad.getTitle());
        assertEquals("This is a test ad description", ad.getDescription());
        assertEquals("image1.jpg", ad.getImageUrl());
    }

    @Test
    void testAdMissingTitle() {
        Ad adWithNoTitle = new Ad(2L, null, "Description without title", "image2.jpg");

        assertNull(adWithNoTitle.getTitle());
        assertNotNull(adWithNoTitle.getDescription());
    }

    @Test
    void testAdMissingDescription() {
        Ad adWithNoDescription = new Ad(3L, "Ad without description", null, "image3.jpg");

        assertNull(adWithNoDescription.getDescription());
        assertNotNull(adWithNoDescription.getTitle());
    }

    @Test
    void testAdWithoutImageUrl() {
        Ad adWithoutImage = new Ad(4L, "Ad without Image", "Description of ad without image", null);

        assertNull(adWithoutImage.getImageUrl());
    }

    @Test
    void testAllArgsConstructorWithFullParameters() {
        Ad ad = new Ad(
                1L,
                "Ad Title",
                "Ad Description",
                "http://image.url",
                "ADMIN",
                false
        );

        assertEquals(1L, ad.getId());
        assertEquals("Ad Title", ad.getTitle());
        assertEquals("Ad Description", ad.getDescription());
        assertEquals("http://image.url", ad.getImageUrl());
        assertEquals("ADMIN", ad.getUserRole());
        assertFalse(ad.isActive());
    }

    @Test
    void testConstructorWithUserRoleOnly() {
        Ad ad = new Ad(
                2L,
                "Second Ad",
                "Description",
                "http://image2.url",
                "ORGANIZER"
        );

        assertEquals(2L, ad.getId());
        assertEquals("Second Ad", ad.getTitle());
        assertEquals("Description", ad.getDescription());
        assertEquals("http://image2.url", ad.getImageUrl());
        assertEquals("ORGANIZER", ad.getUserRole());
        assertTrue(ad.isActive()); // karena default true
    }

    @Test
    void testConstructorWithDefaults() {
        Ad ad = new Ad(
                3L,
                "Default Ad",
                "Default Description",
                "http://image3.url"
        );

        assertEquals("USER", ad.getUserRole());
        assertTrue(ad.isActive());
    }

    @Test
    void testSettersAndGetters() {
        Ad ad = new Ad();
        ad.setId(4L);
        ad.setTitle("Title");
        ad.setDescription("Desc");
        ad.setImageUrl("url");
        ad.setUserRole("MODERATOR");
        ad.setActive(false);

        assertEquals(4L, ad.getId());
        assertEquals("Title", ad.getTitle());
        assertEquals("Desc", ad.getDescription());
        assertEquals("url", ad.getImageUrl());
        assertEquals("MODERATOR", ad.getUserRole());
        assertFalse(ad.isActive());
    }
}
