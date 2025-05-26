package id.ac.ui.cs.advprog.eventspherre.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PromoCodeTest {
    
    private PromoCode promoCode;
    private User organizer;
    
    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1);
        organizer.setName("Organizer One");
        organizer.setEmail("organizer1@test.com");
        
        
        promoCode = PromoCode.builder()
                .id(1)
                .code("SAVE20")
                .description("Save 20% on your ticket")
                .discountPercentage(new BigDecimal("20.00"))
                .validFrom(LocalDate.now().minusDays(1))
                .validUntil(LocalDate.now().plusDays(7))
                .maxUsage(100)
                .currentUsage(0)
                .isActive(true)
                .organizerId(organizer.getId())
                .build();
    }
    
    @Test
    void testPromoCodeCreation() {
        assertNotNull(promoCode);
        assertEquals("SAVE20", promoCode.getCode());
        assertEquals("Save 20% on your ticket", promoCode.getDescription());
        assertEquals(new BigDecimal("20.00"), promoCode.getDiscountPercentage());
        assertEquals(100, promoCode.getMaxUsage());
        assertEquals(0, promoCode.getCurrentUsage());
        assertTrue(promoCode.getIsActive());
        assertEquals(organizer.getId(), promoCode.getOrganizerId());
    }
    
    @Test
    void testIsValidWhenActive() {
        assertTrue(promoCode.isValid());
    }
    
    @Test
    void testIsValidWhenInactive() {
        promoCode.setIsActive(false);
        assertFalse(promoCode.isValid());
    }
    
    @Test
    void testIsValidWhenExpired() {
        promoCode.setValidUntil(LocalDate.now().minusDays(1));
        assertFalse(promoCode.isValid());
    }
    
    @Test
    void testIsValidWhenNotYetStarted() {
        promoCode.setValidFrom(LocalDate.now().plusDays(1));
        assertFalse(promoCode.isValid());
    }
    
    @Test
    void testIsValidWhenMaxUsageReached() {
        promoCode.setCurrentUsage(100);
        assertFalse(promoCode.isValid());
    }
    
    @Test
    void testIncrementUsage() {
        assertEquals(0, promoCode.getCurrentUsage());
        promoCode.incrementUsage();
        assertEquals(1, promoCode.getCurrentUsage());
        promoCode.incrementUsage();
        assertEquals(2, promoCode.getCurrentUsage());
    }
    
    @Test
    void testOnCreate() {
        PromoCode newPromoCode = new PromoCode();
        newPromoCode.onCreate();
        assertNotNull(newPromoCode.getCreatedAt());
        assertNotNull(newPromoCode.getUpdatedAt());
        // Both should be set to "now", but might have microsecond differences
        assertTrue(newPromoCode.getCreatedAt().isEqual(newPromoCode.getUpdatedAt()) ||
                   newPromoCode.getCreatedAt().isBefore(newPromoCode.getUpdatedAt().plusSeconds(1)));
    }
    
    @Test
    void testOnUpdate() throws InterruptedException {
        PromoCode newPromoCode = new PromoCode();
        newPromoCode.onCreate();
        LocalDateTime initialUpdatedAt = newPromoCode.getUpdatedAt();
        
        Thread.sleep(10); // Small delay to ensure time difference
        
        newPromoCode.onUpdate();
        assertNotEquals(initialUpdatedAt, newPromoCode.getUpdatedAt());
        assertTrue(newPromoCode.getUpdatedAt().isAfter(initialUpdatedAt));
    }
}