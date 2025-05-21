package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketTypeService {
    TicketType create(String name, BigDecimal price, int quota, User creator);
    Optional<TicketType> getTicketTypeById(UUID id);
    TicketType updateTicketType(UUID id, TicketType updated, User editor);
    void deleteTicketType(UUID id, User requester);
    List<TicketType> findAll();
}
