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
        return null;
    }

    @Override
    public Optional<TicketType> getTicketTypeById(UUID id) {
        return Optional.empty();
    }

    @Override
    public TicketType updateTicketType(UUID id, TicketType updated, User editor) {
        return null;
    }

    @Override
    public void deleteTicketType(UUID id, User requester) {
    }
}
