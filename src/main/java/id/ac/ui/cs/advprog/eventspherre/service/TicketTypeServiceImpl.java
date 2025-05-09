package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    @Override
    public TicketType createTicketType(TicketType type, User organizer) {
        if (organizer.getRole() != User.Role.ORGANIZER) {
            throw new IllegalArgumentException("Only organizers can create ticket types");
        }
        return ticketTypeRepository.save(type);
    }

    @Override
    public Optional<TicketType> getTicketTypeById(UUID id) {
        return ticketTypeRepository.findById(id);
    }

    @Override
    public TicketType updateTicketType(UUID id, TicketType updated, User editor) {
        TicketType existing = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TicketType not found"));

        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        existing.setQuota(updated.getQuota());

        return ticketTypeRepository.save(existing);
    }

    @Override
    public void deleteTicketType(UUID id, User requester) {
        if (requester.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can delete ticket types");
        }
        ticketTypeRepository.deleteById(id);
    }
}
