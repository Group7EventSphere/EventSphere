package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    @Override
    public TicketType create(String name, BigDecimal price, int quota, User user, int eventId) {
        TicketType ticketType = TicketType.create(name, price, quota, user, eventId);
        return ticketTypeRepository.save(ticketType);
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

        // Step 1: Delete associated tickets
        ticketService.deleteTicketsByTicketTypeId(id);

        if (ticketRepository.existsByTicketTypeId(id)) {
            throw new IllegalStateException("Cannot delete ticket type with existing tickets.");
        }

        // Safe to delete
        ticketTypeRepository.deleteById(id);
    }

    @Override
    public List<TicketType> findAll() {
        return ticketTypeRepository.findAll();
    }

    @Override
    public List<TicketType> findByEventId(int eventId) {
        // Use eventId toString to match with our eventUuid field in the database
        return ticketTypeRepository.findByEventId(eventId);
    }

    @Override
    public void associateWithEvent(UUID ticketTypeId, int eventId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new IllegalArgumentException("TicketType not found"));

        ticketType.setEventId(eventId);
        ticketTypeRepository.save(ticketType);
    }

    @Override
    public List<TicketType> getTicketTypesByEventId(Integer eventId) {
        return ticketTypeRepository.findByEventId(eventId);
    }
}
