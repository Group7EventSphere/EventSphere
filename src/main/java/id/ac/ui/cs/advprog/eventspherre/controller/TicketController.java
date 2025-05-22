package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.TicketService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketTypeService ticketTypeService;
    private final UserService userService;

    public TicketController(TicketService ticketService, TicketTypeService ticketTypeService, UserService userService) {
        this.ticketService = ticketService;
        this.ticketTypeService = ticketTypeService;
        this.userService = userService;
    }

    // Show form to buy a ticket
    @GetMapping("/create")
    public String showTicketForm(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("ticketTypes", ticketTypeService.findAll());
        return "ticket/create";
    }

    // Handle form submission
    @PostMapping
    public String createTicket(@ModelAttribute Ticket ticket, Principal principal, @RequestParam int quota) {
        User attendee = userService.getUserByEmail(principal.getName());
        ticket.setAttendee(attendee);

        List<Ticket> tickets = ticketService.createTicket(ticket, quota);

        // Redirect to confirmation of first ticket (or ticket list, your choice)
        return "redirect:/tickets";
    }

    // Show detail view
    @GetMapping("/{id}")
    public String getTicketById(@PathVariable UUID id, Model model, Principal principal) {
        Optional<Ticket> ticket = ticketService.getTicketById(id);
        ticket.ifPresent(value -> model.addAttribute("ticket", value));
        return ticket.map(t -> "ticket/detail").orElse("redirect:/tickets");
    }

    // List tickets for the logged-in user
    @GetMapping
    public String listUserTickets(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        List<Ticket> tickets = ticketService.getTicketsByAttendeeId(user.getId());
        model.addAttribute("tickets", tickets);
        return "ticket/list";
    }

    // Get tickets by attendee (API)
    @GetMapping("/attendee/{attendeeId}")
    @ResponseBody
    public ResponseEntity<List<Ticket>> getTicketsByAttendee(@PathVariable Integer attendeeId) {
        List<Ticket> tickets = ticketService.getTicketsByAttendeeId(attendeeId);
        return ResponseEntity.ok(tickets);
    }

    // Get ticket by confirmation code (API)
    @GetMapping("/code/{confirmationCode}")
    @ResponseBody
    public ResponseEntity<Ticket> getByConfirmationCode(@PathVariable String confirmationCode) {
        Optional<Ticket> ticket = ticketService.getTicketByConfirmationCode(confirmationCode);
        return ticket.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Count tickets by ticket type (API)
    @GetMapping("/count/{ticketTypeId}")
    @ResponseBody
    public ResponseEntity<Long> countByType(@PathVariable UUID ticketTypeId) {
        long count = ticketService.countTicketsByType(ticketTypeId);
        return ResponseEntity.ok(count);
    }

    // Delete ticket (API)
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    // Update ticket (API)
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Ticket> updateTicket(@PathVariable UUID id, @RequestBody Ticket updatedTicket, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        Ticket updated = ticketService.updateTicket(id, updatedTicket);
        return ResponseEntity.ok(updated);
    }
}