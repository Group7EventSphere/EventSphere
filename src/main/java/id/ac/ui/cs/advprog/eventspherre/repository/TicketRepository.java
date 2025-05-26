package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findAllByUserId(Integer userId);
    Optional<Ticket> findByConfirmationCode(String confirmationCode);
    long countByTicketTypeId(UUID ticketTypeId);
    boolean existsByTicketTypeId(UUID ticketTypeId);
    void deleteByTicketTypeId(UUID ticketTypeId);
    List<Ticket> findByTransactionId(UUID transactionId);
    void deleteByTransactionId(UUID transactionId);
}
