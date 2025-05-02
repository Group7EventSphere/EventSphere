package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.model.UnauthorizedAccessException;

public class AdService {

    private final String currentUserRole;

    public AdService(String role) {
        this.currentUserRole = role;
    }

    public Ad createAd(Ad ad) {
        if ("ADMIN".equalsIgnoreCase(currentUserRole)) {
            return ad;
        } else {
            throw new UnauthorizedAccessException("Only admins can create ads.");
        }
    }

    public Ad updateAd(Long id, Ad updatedAd) {
        if ("ADMIN".equalsIgnoreCase(currentUserRole)) {
            updatedAd.setId(id);
            return updatedAd;
        } else {
            throw new UnauthorizedAccessException("Only admins can edit ads.");
        }
    }

    public Ad viewAd(Long id) {
        return new Ad(id, "Sample Ad", "Sample description", "image.jpg", currentUserRole);
    }
}
