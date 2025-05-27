package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.dto.AdRequestDto;
import id.ac.ui.cs.advprog.eventspherre.dto.AdResponseDTO;
import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.repository.AdRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class AdService {

    private final AdRepository adRepository;
    private final WebClient webClient;

    public AdService(WebClient.Builder webClientBuilder, AdRepository adRepository) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api").build();
        this.adRepository = adRepository;
    }


    public AdResponseDTO addAdFromRest(AdRequestDto dto) {
        AdResponseDTO response = webClient.post()
                .uri("/api/ads")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(AdResponseDTO.class)   // langsung parsing ke class-nya
                .block();

        if (response == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No response body from /api/ads");
        }
        return response;
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
