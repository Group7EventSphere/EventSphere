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
    void testAdToString() {
        String expectedString = "Ad [id=1, title=Test Ad, description=This is a test ad description, imageUrl=image1.jpg]";
        assertEquals(expectedString, ad.toString());
    }
}
