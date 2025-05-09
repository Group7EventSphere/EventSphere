package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        Ticket created = ticketService.createTicket(ticket);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable UUID id) {
        Optional<Ticket> ticket = ticketService.getTicketById(id);
        return ticket.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/attendee/{attendeeId}")
    public ResponseEntity<List<Ticket>> getTicketsByAttendee(@PathVariable Integer attendeeId) {
        List<Ticket> tickets = ticketService.getTicketsByAttendeeId(attendeeId);
        return ResponseEntity.ok(tickets);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/code/{confirmationCode}")
    public ResponseEntity<Ticket> getByConfirmationCode(@PathVariable String confirmationCode) {
        Optional<Ticket> ticket = ticketService.getTicketByConfirmationCode(confirmationCode);
        return ticket.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/count/{ticketTypeId}")
    public ResponseEntity<Long> countByType(@PathVariable UUID ticketTypeId) {
        long count = ticketService.countTicketsByType(ticketTypeId);
        return ResponseEntity.ok(count);
    }
}
