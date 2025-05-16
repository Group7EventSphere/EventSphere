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

    public Ad createAd(Ad ad) {
        return adRepository.save(ad);
    }

    public Ad getAd(Long id) {
        Optional<Ad> opt = adRepository.findById(id);
        return opt.orElse(null);
    }

    public List<Ad> getAllAds() {
        return adRepository.findAll();
    }

    public Ad updateAd(Long id, Ad ad) {
        if (!adRepository.existsById(id)) {
            return null;
        }
        ad.setId(id);
        return adRepository.save(ad);
    }

    public void deleteAd(Long id) {
        adRepository.deleteById(id);
    }
}
