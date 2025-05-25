package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Integer> {
    Optional<PromoCode> findByCode(String code);
    
    List<PromoCode> findByOrganizerId(Integer organizerId);
    
    List<PromoCode> findByOrganizerIdAndIsActive(Integer organizerId, Boolean isActive);
    
    @Query("SELECT p FROM PromoCode p WHERE p.validUntil < :today AND p.isActive = true")
    List<PromoCode> findExpiredPromoCodes(LocalDate today);
    
    @Query("SELECT p FROM PromoCode p WHERE p.organizerId = :organizerId AND " +
           "(LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<PromoCode> searchByOrganizerIdAndKeyword(Integer organizerId, String search);
    
    boolean existsByCode(String code);
}