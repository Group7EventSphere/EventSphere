package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.*;
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
        // Validate ticketType
        TicketType ticketType = ticket.getTicketType();
        if (ticketType.getQuota() <= 0) {
            throw new IllegalStateException("Ticket quota is full");
        }

        // Reduce quota (business logic)
        ticketType.reduceQuota(1);  // Will throw if not enough quota
        // If TicketType is a managed JPA entity, make sure it's saved

        // Extract user info from ticket
        User attendee = ticket.getAttendee();
        if (attendee == null || attendee.getId() == null || attendee.getId() <= 0) {
            throw new IllegalArgumentException("Attendee must be specified with a valid user ID");
        }

        // Set user ID from the attendee
        ticket.setUserId(attendee.getId());

        // Generate confirmation code if not already set
        if (ticket.getConfirmationCode() == null || ticket.getConfirmationCode().isEmpty()) {
            String confirmationCode = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            ticket.setConfirmationCode(confirmationCode);
        }

        return ticketRepository.save(ticket);
    }

    @Override
    public Optional<Ticket> getTicketById(UUID id) {
        return ticketRepository.findById(id);
    }

    @Override
    public List<Ticket> getTicketsByAttendeeId(Integer attendeeId) {
        return ticketRepository.findAllByUserId(attendeeId);
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

