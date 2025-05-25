package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.PromoCode;
import id.ac.ui.cs.advprog.eventspherre.model.User;

import java.util.List;

public interface PromoCodeService {
    PromoCode createPromoCode(PromoCode promoCode, User organizer);
    
    PromoCode getPromoCodeById(Integer id);
    
    PromoCode getPromoCodeByCode(String code);
    
    List<PromoCode> getAllPromoCodesByOrganizer(User organizer);
    
    List<PromoCode> getActivePromoCodesByOrganizer(User organizer);
    
    List<PromoCode> searchPromoCodesByOrganizer(User organizer, String keyword);
    
    PromoCode updatePromoCode(Integer id, PromoCode updatedPromoCode, User organizer);
    
    void deletePromoCode(Integer id, User organizer);
    
    boolean isPromoCodeValid(String code);
    
    void deactivateExpiredPromoCodes();
    
    boolean isCodeAvailable(String code);
}