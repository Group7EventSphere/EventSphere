package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdService {

    private final AdRepository adRepository;

    @Autowired
    public AdService(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    // Create a new ad
    public Ad createAd(Ad ad) {
        return adRepository.save(ad);
    }

    // Get an ad by ID
    public Ad getAd(Long id) {
        Optional<Ad> ad = adRepository.findById(id);
        return ad.orElse(null);  // Return the ad if found, else return null
    }

    // Get all ads
    public List<Ad> getAllAds() {
        return (List<Ad>) adRepository.findAll();  // Convert Iterable to List
    }

    // Update an existing ad
    public Ad updateAd(Long id, Ad ad) {
        if (adRepository.existsById(id)) {
            ad.setId(id);  // Ensure the ID is set to the existing ad ID
            return adRepository.save(ad);
        }
        return null;  // Return null if ad with given ID doesn't exist
    }

    // Delete an ad
    public void deleteAd(Long id) {
        adRepository.deleteById(id);
    }
}
