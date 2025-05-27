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
import id.ac.ui.cs.advprog.eventspherre.service.PromoCodeService;
import id.ac.ui.cs.advprog.eventspherre.model.PromoCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
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
    private final PromoCodeService promoCodeService;

    public TicketController(TicketService ticketService, TicketTypeService ticketTypeService, 
                           UserService userService, EventManagementService eventManagementService,
                           PaymentHandler paymentHandler, PaymentService paymentService,
                           PromoCodeService promoCodeService) {
        this.ticketService = ticketService;
        this.ticketTypeService = ticketTypeService;
        this.userService = userService;
        this.eventManagementService = eventManagementService;
        this.paymentHandler = paymentHandler;
        this.paymentService = paymentService;
        this.promoCodeService = promoCodeService;
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
                               @RequestParam(value = "promoCode", required = false) String promoCode,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        User attendee = userService.getUserByEmail(principal.getName());        // Get TicketType from service (re-hydration)
        TicketType ticketType = ticketTypeService.getTicketTypeById(ticketTypeId)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ERROR_INVALID_TICKET_TYPE_ID));

        // Calculate base total price
        BigDecimal basePrice = ticketType.getPrice().multiply(new BigDecimal(quota));
        double totalPrice = basePrice.doubleValue();
        
        // Apply promo code discount if provided
        PromoCode appliedPromoCode = null;
        if (promoCode != null && !promoCode.trim().isEmpty()) {
            try {
                appliedPromoCode = promoCodeService.getPromoCodeByCode(promoCode.trim());
                if (appliedPromoCode.isValid()) {
                    BigDecimal discountAmount = basePrice.multiply(appliedPromoCode.getDiscountPercentage().divide(new BigDecimal(100)));
                    totalPrice = basePrice.subtract(discountAmount).doubleValue();
                } else {
                    redirectAttributes.addFlashAttribute("error", "Invalid or expired promo code");
                    redirectAttributes.addAttribute("ticketTypeId", ticketTypeId);
                    redirectAttributes.addAttribute("quota", quota);
                    return AppConstants.REDIRECT_TICKETS_CREATE;
                }
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", "Promo code not found");
                redirectAttributes.addAttribute("ticketTypeId", ticketTypeId);
                redirectAttributes.addAttribute("quota", quota);
                return AppConstants.REDIRECT_TICKETS_CREATE;
            }
        }

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
            ticket.setTransactionId(transaction.getId());
            
            // Set price information
            ticket.setOriginalPrice(ticketType.getPrice());
            ticket.setPurchasePrice(new BigDecimal(totalPrice / quota)); // Price per ticket after discount
            // Increment promo code usage if one was applied
            if (appliedPromoCode != null) {
                ticket.setDiscountPercentage(appliedPromoCode.getDiscountPercentage());
                appliedPromoCode.incrementUsage();
                promoCodeService.updatePromoCode(appliedPromoCode.getId(), appliedPromoCode, 
                    userService.getUserById(appliedPromoCode.getOrganizerId()));
            } else {
                ticket.setDiscountPercentage(BigDecimal.ZERO);
            }
            
            // Create multiple tickets
            ticketService.createTicket(ticket, quota);

            String successMessage = appliedPromoCode != null ? 
                String.format(AppConstants.SUCCESS_TICKET_PURCHASED + " with promo code applied!", quota) :
                String.format(AppConstants.SUCCESS_TICKET_PURCHASED, quota);
            redirectAttributes.addFlashAttribute("message", successMessage);
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

        // Group tickets by transaction ID to get quantity
        Map<UUID, List<Ticket>> ticketsByTransaction = new HashMap<>();
        for (Ticket ticket : tickets) {
            if (ticket.getTransactionId() != null) {
                ticketsByTransaction.computeIfAbsent(ticket.getTransactionId(), k -> new ArrayList<>()).add(ticket);
            } else {
                // Handle tickets without transaction ID (legacy data)
                ticketsByTransaction.put(ticket.getId(), Collections.singletonList(ticket));
            }
        }

        List<Map<String, Object>> ticketWithEventList = new ArrayList<>();
        for (Map.Entry<UUID, List<Ticket>> entry : ticketsByTransaction.entrySet()) {
            List<Ticket> transactionTickets = entry.getValue();
    
            if (!transactionTickets.isEmpty()) {
                Ticket firstTicket = transactionTickets.get(0);
                TicketType type = firstTicket.getTicketType();
        
                if (type != null) {
                    Integer eventId = type.getEventId();
                    Event event = eventManagementService.getEvent(eventId);
            
                    if (event != null) {
                        Map<String, Object> ticketEntry = new HashMap<>();
                        ticketEntry.put(ModelAttributes.TICKET, firstTicket);
                        ticketEntry.put(ModelAttributes.EVENT, event);
                        ticketEntry.put("quantity", transactionTickets.size());
                        ticketEntry.put("tickets", transactionTickets); // All tickets in this transaction
                        ticketWithEventList.add(ticketEntry);
                    }
                }
            }
        }

        model.addAttribute("ticketWithEventList", ticketWithEventList);
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
    
    // Validate promo code (API)
    @PostMapping("/validate-promo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validatePromoCode(@RequestParam("promoCode") String promoCode,
                                                                @RequestParam("ticketPrice") BigDecimal ticketPrice,
                                                                @RequestParam("quota") int quota) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Validating promo code: " + promoCode + ", ticketPrice: " + ticketPrice + ", quota: " + quota);
            PromoCode promo = promoCodeService.getPromoCodeByCode(promoCode.trim());
            if (promo.isValid()) {
                BigDecimal basePrice = ticketPrice.multiply(new BigDecimal(quota));
                BigDecimal discountAmount = basePrice.multiply(promo.getDiscountPercentage().divide(new BigDecimal(100)));
                BigDecimal finalPrice = basePrice.subtract(discountAmount);
                
                response.put("valid", true);
                response.put("discountPercentage", promo.getDiscountPercentage());
                response.put("discountAmount", discountAmount);
                response.put("finalPrice", finalPrice);
                response.put("message", "Promo code applied successfully!");
            } else {
                response.put("valid", false);
                response.put("message", "Promo code is invalid or expired");
            }
        } catch (IllegalArgumentException e) {
            response.put("valid", false);
            response.put("message", "Promo code not found");
        }
        
        return ResponseEntity.ok(response);
    }
}