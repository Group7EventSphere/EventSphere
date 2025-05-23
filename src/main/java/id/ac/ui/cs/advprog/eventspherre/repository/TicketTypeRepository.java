package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {
    boolean existsById(UUID id);
    List<TicketType> findByEventId(int eventId);
}
