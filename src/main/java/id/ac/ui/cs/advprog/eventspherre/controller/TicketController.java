package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.TicketService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketTypeService ticketTypeService;

    // Show form to buy a ticket
    @GetMapping("/create")
    public String showTicketForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("ticketTypes", ticketTypeService.findAll());
        return "ticket/create";
    }

    // Handle form submission
    @PostMapping
    public String createTicket(@ModelAttribute Ticket ticket,
                               @SessionAttribute("loggedInUser") User user) {
        ticket.setAttendee(user);
        ticketService.createTicket(ticket);
        return "redirect:/tickets";
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

    // List tickets for the logged-in user
    @GetMapping
    public String listUserTickets(Model model,
                                  @SessionAttribute("loggedInUser") User user) {
        List<Ticket> myTickets = ticketService.getTicketsByAttendeeId(user.getId());
        model.addAttribute("tickets", myTickets);
        return "ticket/list";
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
