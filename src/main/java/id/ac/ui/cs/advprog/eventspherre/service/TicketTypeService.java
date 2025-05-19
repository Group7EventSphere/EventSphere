package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;

import java.util.Optional;
import java.util.UUID;

public interface TicketTypeService {
    Optional<TicketType> getTicketTypeById(UUID id);
    TicketType updateTicketType(UUID id, TicketType updated, User editor);
    void deleteTicketType(UUID id, User requester);
}
