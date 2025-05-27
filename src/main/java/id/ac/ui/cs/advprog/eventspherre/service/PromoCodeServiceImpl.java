package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.PromoCode;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PromoCodeServiceImpl implements PromoCodeService {
    
    private final PromoCodeRepository promoCodeRepository;
    
    @Override
    public PromoCode createPromoCode(PromoCode promoCode, User organizer) {
        if (promoCodeRepository.existsByCode(promoCode.getCode())) {
            throw new IllegalArgumentException("Promo code already exists");
        }
        
        promoCode.setOrganizerId(organizer.getId());
        promoCode.setCurrentUsage(0);
        promoCode.setIsActive(true);
        
        return promoCodeRepository.save(promoCode);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PromoCode getPromoCodeById(Integer id) {
        return promoCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promo code not found"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public PromoCode getPromoCodeByCode(String code) {
        return promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Promo code not found"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PromoCode> getAllPromoCodesByOrganizer(User organizer) {
        return promoCodeRepository.findByOrganizerId(organizer.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PromoCode> getActivePromoCodesByOrganizer(User organizer) {
        return promoCodeRepository.findByOrganizerIdAndIsActive(organizer.getId(), true);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PromoCode> searchPromoCodesByOrganizer(User organizer, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPromoCodesByOrganizer(organizer);
        }
        return promoCodeRepository.searchByOrganizerIdAndKeyword(organizer.getId(), keyword);
    }
    
    @Override
    public PromoCode updatePromoCode(Integer id, PromoCode updatedPromoCode, User organizer) {
        PromoCode existingPromoCode = getPromoCodeById(id);
        
        if (!existingPromoCode.getOrganizerId().equals(organizer.getId())) {
            throw new IllegalArgumentException("You are not authorized to update this promo code");
        }
        
        if (!existingPromoCode.getCode().equals(updatedPromoCode.getCode()) && 
            promoCodeRepository.existsByCode(updatedPromoCode.getCode())) {
            throw new IllegalArgumentException("Promo code already exists");
        }
        
        existingPromoCode.setCode(updatedPromoCode.getCode());
        existingPromoCode.setDescription(updatedPromoCode.getDescription());
        existingPromoCode.setDiscountPercentage(updatedPromoCode.getDiscountPercentage());
        existingPromoCode.setValidFrom(updatedPromoCode.getValidFrom());
        existingPromoCode.setValidUntil(updatedPromoCode.getValidUntil());
        existingPromoCode.setMaxUsage(updatedPromoCode.getMaxUsage());
        existingPromoCode.setIsActive(updatedPromoCode.getIsActive());
        
        return promoCodeRepository.save(existingPromoCode);
    }
    
    @Override
    public void deletePromoCode(Integer id, User organizer) {
        PromoCode promoCode = getPromoCodeById(id);
        
        if (!promoCode.getOrganizerId().equals(organizer.getId())) {
            throw new IllegalArgumentException("You are not authorized to delete this promo code");
        }
        
        promoCodeRepository.delete(promoCode);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isPromoCodeValid(String code) {
        return promoCodeRepository.findByCode(code)
                .map(PromoCode::isValid)
                .orElse(false);
    }
    
    @Override
    public void deactivateExpiredPromoCodes() {
        List<PromoCode> expiredPromoCodes = promoCodeRepository.findExpiredPromoCodes(LocalDate.now());
        expiredPromoCodes.forEach(promoCode -> {
            promoCode.setIsActive(false);
            promoCodeRepository.save(promoCode);
        });
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isCodeAvailable(String code) {
        return !promoCodeRepository.existsByCode(code);
    }
}