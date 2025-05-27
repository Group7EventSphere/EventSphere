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
        Ad underTest = new Ad(2L, null, "Description without title", "image2.jpg");

        assertNull(underTest.getTitle());
        assertNotNull(underTest.getDescription());
    }

    @Test
    void testAdMissingDescription() {
        Ad underTest = new Ad(3L, "Ad without description", null, "image3.jpg");

        assertNull(underTest.getDescription());
        assertNotNull(underTest.getTitle());
    }

    @Test
    void testAdWithoutImageUrl() {
        Ad underTest = new Ad(4L, "Ad without Image", "Description of ad without image", null);

        assertNull(underTest.getImageUrl());
    }

    @Test
    void testAllArgsConstructorWithFullParameters() {
        Ad underTest = new Ad(
                1L,
                "Ad Title",
                "Ad Description",
                "http://image.url",
                "ADMIN",
                false
        );

        assertEquals(1L, underTest.getId());
        assertEquals("Ad Title", underTest.getTitle());
        assertEquals("Ad Description", underTest.getDescription());
        assertEquals("http://image.url", underTest.getImageUrl());
        assertEquals("ADMIN", underTest.getUserRole());
        assertFalse(underTest.isActive());
    }

    @Test
    void testConstructorWithUserRoleOnly() {
        Ad underTest = new Ad(
                2L,
                "Second Ad",
                "Description",
                "http://image2.url",
                "ORGANIZER"
        );

        assertEquals(2L, underTest.getId());
        assertEquals("Second Ad", underTest.getTitle());
        assertEquals("Description", underTest.getDescription());
        assertEquals("http://image2.url", underTest.getImageUrl());
        assertEquals("ORGANIZER", underTest.getUserRole());
        assertTrue(underTest.isActive()); // because default is true
    }

    @Test
    void testConstructorWithDefaults() {
        Ad underTest = new Ad(
                3L,
                "Default Ad",
                "Default Description",
                "http://image3.url"
        );

        assertEquals(3L, underTest.getId());
        assertEquals("Default Ad", underTest.getTitle());
        assertEquals("Default Description", underTest.getDescription());
        assertEquals("http://image3.url", underTest.getImageUrl());
        assertEquals("USER", underTest.getUserRole());
        assertTrue(underTest.isActive());
    }

    @Test
    void testSettersAndGetters() {
        Ad underTest = new Ad();
        underTest.setId(4L);
        underTest.setTitle("Title");
        underTest.setDescription("Desc");
        underTest.setImageUrl("url");
        underTest.setUserRole("MODERATOR");
        underTest.setActive(false);

        assertEquals(4L, underTest.getId());
        assertEquals("Title", underTest.getTitle());
        assertEquals("Desc", underTest.getDescription());
        assertEquals("url", underTest.getImageUrl());
        assertEquals("MODERATOR", underTest.getUserRole());
        assertFalse(underTest.isActive());
    }
}
