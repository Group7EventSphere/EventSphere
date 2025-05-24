package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketTypeService ticketTypeService;
    private final UserService userService;
    private final EventManagementService eventManagementService;

    public TicketController(TicketService ticketService, TicketTypeService ticketTypeService, UserService userService, EventManagementService eventManagementService) {
        this.ticketService = ticketService;
        this.ticketTypeService = ticketTypeService;
        this.userService = userService;
        this.eventManagementService = eventManagementService;
    }

    @GetMapping("/select/{eventId}")
    public String showTicketSelection(@PathVariable("eventId") int eventId, Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        Event event = eventManagementService.getEvent(eventId);
        if (event == null) {
            return "redirect:/events"; // or show error page
        }

        List<TicketType> ticketTypes = ticketTypeService.findByEventId(eventId);

        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypes);
        return "ticket/select"; // same select.html
    }

    @PostMapping("/select")
    public String handleTicketSelection(@RequestParam("ticketTypeId") UUID ticketTypeId,
                                        @RequestParam("quota") int quota,
                                        RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("ticketTypeId", ticketTypeId);
        redirectAttributes.addAttribute("quota", quota);
        return "redirect:/tickets/create";
    }

    @GetMapping("/create")
    public String showTicketForm(@RequestParam("ticketTypeId") UUID ticketTypeId,
                                 @RequestParam("quota") int quota,
                                 Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());

        // Get the selected ticket type
        TicketType ticketType = ticketTypeService.getTicketTypeById(ticketTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket type ID"));

        Event event = eventManagementService.getEvent(ticketType.getEventId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime parsedDate = LocalDateTime.parse(event.getEventDate(), formatter);

        // Prepare ticket
        Ticket ticket = new Ticket();
        ticket.setTicketType(ticketType);
        ticket.setAttendee(user);

        model.addAttribute("ticket", ticket);
        model.addAttribute("quota", quota);
        model.addAttribute("ticketType", ticketType);

        // Handling events
        model.addAttribute("event", event);
        model.addAttribute("eventDateFormatted", parsedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm")));

        return "ticket/create";
    }

    @PostMapping("/create")
    public String createTicket(@RequestParam("ticketTypeId") UUID ticketTypeId,
                               @RequestParam("quota") int quota,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        User attendee = userService.getUserByEmail(principal.getName());

        // Get TicketType from service (re-hydration)
        TicketType ticketType = ticketTypeService.getTicketTypeById(ticketTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket type ID"));

        // Build the ticket
        Ticket ticket = new Ticket();
        ticket.setAttendee(attendee);
        ticket.setTicketType(ticketType);

        // Create multiple tickets
        List<Ticket> tickets = ticketService.createTicket(ticket, quota);
        redirectAttributes.addFlashAttribute("message", "Successfully purchased " + quota + " ticket(s).");

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