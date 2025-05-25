package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.*;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventspherre.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;


    @Override
    public List<Ticket> createTicket(Ticket ticket, int quota) {
        TicketType ticketType = ticket.getTicketType();        TicketType existingType = ticketTypeRepository.findById(ticketType.getId())
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ERROR_TICKET_TYPE_NOT_FOUND_SERVICE));

        if (existingType.getQuota() < quota) {
            throw new IllegalStateException(AppConstants.ERROR_NOT_ENOUGH_TICKETS);
        }

        existingType.setQuota(existingType.getQuota() - quota);
        ticketTypeRepository.save(existingType);        User attendee = ticket.getAttendee();
        if (attendee == null || attendee.getId() == null || attendee.getId() <= AppConstants.MIN_VALID_ID) {
            throw new IllegalArgumentException(AppConstants.ERROR_ATTENDEE_MUST_BE_SPECIFIED);
        }

        List<Ticket> tickets = new ArrayList<>();

        for (int i = 0; i < quota; i++) {
            Ticket t = new Ticket();
            t.setTicketType(existingType);
            t.setAttendee(attendee);
            t.setUserId(attendee.getId());
            t.setDate(LocalDate.now());
            t.setConfirmationCode(AppConstants.TICKET_CODE_PREFIX + UUID.randomUUID().toString().substring(0, AppConstants.CONFIRMATION_CODE_LENGTH).toUpperCase());
            t.setTransactionId(ticket.getTransactionId()); // Copy transaction ID from passed ticket
            tickets.add(ticketRepository.save(t));
        }

        return tickets;
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
    public void deleteTicketsByTicketTypeId(UUID ticketTypeId) {
        ticketRepository.deleteByTicketTypeId(ticketTypeId);
    }

    @Override
    public Optional<Ticket> getTicketByConfirmationCode(String code) {
        return ticketRepository.findByConfirmationCode(code);
    }

    @Override
    public long countTicketsByType(UUID ticketTypeId) {
        return ticketRepository.countByTicketTypeId(ticketTypeId);
    }

    @Override
    public Ticket updateTicket(UUID id, Ticket updatedTicket) {        // Make sure ticket exists
        Ticket existing = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ERROR_TICKET_NOT_FOUND_SERVICE));

        // Update fields (except ID and attendee which should stay the same)
        existing.setTicketType(updatedTicket.getTicketType());
        existing.setConfirmationCode(updatedTicket.getConfirmationCode());

        return ticketRepository.save(existing);
    }
}

