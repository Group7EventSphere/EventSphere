package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import id.ac.ui.cs.advprog.eventspherre.common.ModelAttributes;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.Ticket;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentRequest;
import id.ac.ui.cs.advprog.eventspherre.model.PaymentTransaction;
import id.ac.ui.cs.advprog.eventspherre.handler.PaymentHandler;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import id.ac.ui.cs.advprog.eventspherre.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketTypeService ticketTypeService;
    private final UserService userService;
    private final EventManagementService eventManagementService;
    private final PaymentHandler paymentHandler;
    private final PaymentService paymentService;

    public TicketController(TicketService ticketService, TicketTypeService ticketTypeService, 
                           UserService userService, EventManagementService eventManagementService,
                           PaymentHandler paymentHandler, PaymentService paymentService) {
        this.ticketService = ticketService;
        this.ticketTypeService = ticketTypeService;
        this.userService = userService;
        this.eventManagementService = eventManagementService;
        this.paymentHandler = paymentHandler;
        this.paymentService = paymentService;
    }

    @GetMapping("/select/{eventId}")
    public String showTicketSelection(@PathVariable("eventId") int eventId, Model model, Principal principal) {        userService.getUserByEmail(principal.getName());
        Event event = eventManagementService.getEvent(eventId);
        if (event == null) {
            return AppConstants.REDIRECT_EVENTS;
        }

        List<TicketType> ticketTypes = ticketTypeService.findByEventId(eventId);

        model.addAttribute(ModelAttributes.EVENT, event);
        model.addAttribute("ticketTypes", ticketTypes);
        return AppConstants.VIEW_TICKET_SELECT;
    }    @PostMapping("/select")
    public String handleTicketSelection(@RequestParam("ticketTypeId") UUID ticketTypeId,
                                        @RequestParam("quota") int quota,
                                        RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("ticketTypeId", ticketTypeId);
        redirectAttributes.addAttribute("quota", quota);
        return AppConstants.REDIRECT_TICKETS_CREATE;
    }

    @GetMapping("/create")
    public String showTicketForm(@RequestParam("ticketTypeId") UUID ticketTypeId,
                                 @RequestParam("quota") int quota,
                                 Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());        // Get the selected ticket type
        TicketType ticketType = ticketTypeService.getTicketTypeById(ticketTypeId)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ERROR_INVALID_TICKET_TYPE_ID));

        Event event = eventManagementService.getEvent(ticketType.getEventId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime parsedDate = LocalDateTime.parse(event.getEventDate(), formatter);

        // Prepare ticket
        Ticket ticket = new Ticket();
        ticket.setTicketType(ticketType);
        ticket.setAttendee(user);

        model.addAttribute(ModelAttributes.TICKET, ticket);
        model.addAttribute("quota", quota);
        model.addAttribute("ticketType", ticketType);

        // Handling events
        model.addAttribute(ModelAttributes.EVENT, event);        model.addAttribute("eventDateFormatted", parsedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm")));

        return AppConstants.VIEW_TICKET_CREATE;
    }

    @PostMapping("/create")
    public String createTicket(@RequestParam("ticketTypeId") UUID ticketTypeId,
                               @RequestParam("quota") int quota,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        User attendee = userService.getUserByEmail(principal.getName());        // Get TicketType from service (re-hydration)
        TicketType ticketType = ticketTypeService.getTicketTypeById(ticketTypeId)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ERROR_INVALID_TICKET_TYPE_ID));

        // Calculate total price
        double totalPrice = ticketType.getPrice().multiply(new java.math.BigDecimal(quota)).doubleValue();

        // Create payment request for the purchase
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .userId(attendee.getId())
                .amount(totalPrice)
                .type(PaymentRequest.PaymentType.PURCHASE)
                .processed(false)
                .createdAt(java.time.Instant.now())
                .build();
        paymentRequest.setUser(attendee);

        // Process payment through the chain
        paymentHandler.handle(paymentRequest);

        // Wait a bit for the async handler to process
        try {
            Thread.sleep(AppConstants.ASYNC_PROCESSING_DELAY); // Give time for async processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if payment was successful
        if (paymentRequest.isProcessed()) {
            
            // Convert to transaction first to get the transaction ID
            PaymentTransaction transaction = paymentService.persistRequestAndConvert(paymentRequest, "SUCCESS");
            
            // Payment successful, create tickets with transaction ID
            Ticket ticket = new Ticket();
            ticket.setAttendee(attendee);
            ticket.setTicketType(ticketType);
            ticket.setTransactionId(transaction.getId());            // Create multiple tickets
            ticketService.createTicket(ticket, quota);

            redirectAttributes.addFlashAttribute("message", String.format(AppConstants.SUCCESS_TICKET_PURCHASED, quota));
            return AppConstants.REDIRECT_TICKETS;
        } else {
            // Payment failed - insufficient balance
            redirectAttributes.addFlashAttribute("error", AppConstants.ERROR_INSUFFICIENT_BALANCE);
            redirectAttributes.addAttribute("ticketTypeId", ticketTypeId);
            redirectAttributes.addAttribute("quota", quota);
            return AppConstants.REDIRECT_TICKETS_CREATE;
        }
    }    // Show detail view
    @GetMapping("/{id}")
    public String getTicketById(@PathVariable UUID id, Model model, Principal principal) {
        Optional<Ticket> ticket = ticketService.getTicketById(id);
        ticket.ifPresent(value -> model.addAttribute(ModelAttributes.TICKET, value));
        return ticket.map(t -> AppConstants.VIEW_TICKET_DETAIL).orElse(AppConstants.REDIRECT_TICKETS);
    }

    // List tickets for the logged-in user
    @GetMapping
    public String listUserTickets(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        List<Ticket> tickets = ticketService.getTicketsByAttendeeId(user.getId());

        List<Map<String, Object>> ticketWithEventList = tickets.stream()
                .map(ticket -> {
                    TicketType type = ticket.getTicketType();
                    if (type == null) return null;

                    Integer eventId = type.getEventId();
                    Event event = eventManagementService.getEvent(eventId);
                    if (event == null) return null;

                    Map<String, Object> entry = new HashMap<>();
                    entry.put(ModelAttributes.TICKET, ticket);
                    entry.put(ModelAttributes.EVENT, event);
                    return entry;
                })
                .filter(Objects::nonNull)
                .toList();        model.addAttribute("ticketWithEventList", ticketWithEventList);
        return AppConstants.VIEW_TICKET_LIST;
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
        userService.getUserByEmail(principal.getName());
        Ticket updated = ticketService.updateTicket(id, updatedTicket);
        return ResponseEntity.ok(updated);
    }
}