package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketService {
    List<Ticket> createTicket(Ticket ticket, int quantity);
    Optional<Ticket> getTicketById(UUID id);
    List<Ticket> getTicketsByAttendeeId(Integer attendeeId);
    void deleteTicket(UUID id);
    void deleteTicketsByTicketTypeId(UUID ticketTypeId);
    Optional<Ticket> getTicketByConfirmationCode(String code);
    long countTicketsByType(UUID ticketTypeId);
    Ticket updateTicket(UUID id, Ticket ticket);
}
