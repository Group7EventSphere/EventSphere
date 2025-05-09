package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Override
    public Optional<Ticket> getTicketById(UUID id) {
        return ticketRepository.findById(id);
    }

    @Override
    public List<Ticket> getTicketsByAttendeeId(Integer attendeeId) {
        return ticketRepository.findAllByAttendeeId(attendeeId);
    }

    @Override
    public void deleteTicket(UUID id) {
        ticketRepository.deleteById(id);
    }

    @Override
    public Optional<Ticket> getTicketByConfirmationCode(String code) {
        return ticketRepository.findByConfirmationCode(code);
    }

    @Override
    public long countTicketsByType(UUID ticketTypeId) {
        return ticketRepository.countByTicketTypeId(ticketTypeId);
    }
}

