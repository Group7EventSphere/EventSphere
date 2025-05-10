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

    /** Called by createAd controller test */
    public Ad createAd(Ad ad) {
        return adRepository.save(ad);
    }

    /** Called by getAd controller & service tests */
    public Ad getAd(Long id) {
        Optional<Ad> opt = adRepository.findById(id);
        return opt.orElse(null);
    }

    /** Called by getAllAds controller test */
    public List<Ad> getAllAds() {
        return adRepository.findAll();
    }

    /** Called by updateAd controller & service tests */
    public Ad updateAd(Long id, Ad ad) {
        if (!adRepository.existsById(id)) {
            return null;
        }
        ad.setId(id);
        return adRepository.save(ad);
    }

    /** Called by deleteAd controller & service tests */
    public void deleteAd(Long id) {
        adRepository.deleteById(id);
    }
}
