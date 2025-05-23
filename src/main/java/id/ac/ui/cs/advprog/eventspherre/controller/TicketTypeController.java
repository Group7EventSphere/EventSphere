package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/ticket-types")
public class TicketTypeController {

    @Autowired
    private final TicketTypeService ticketTypeService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventManagementService eventManagementService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public String showTicketOverview(Model model, Principal principal) {
        String userEmail = principal.getName();
        User user = userService.getUserByEmail(userEmail);

        List<Event> allEvents = eventManagementService.getAllEvents();
        List<TicketType> allTicketTypes = ticketTypeService.findAll();

        Map<Integer, List<TicketType>> ticketsByEventId = new HashMap<>();
        for (TicketType type : allTicketTypes) {
            int eventId = type.getEventId();
            ticketsByEventId.computeIfAbsent(eventId, k -> new ArrayList<>()).add(type);
        }

        List<Map<String, Object>> eventTicketList = new ArrayList<>();
        for (Event event : allEvents) {
            Map<String, Object> map = new HashMap<>();
            map.put("event", event);
            map.put("ticketTypes", ticketsByEventId.getOrDefault(event.getId(), List.of()));
            eventTicketList.add(map);
        }

        model.addAttribute("eventTicketList", eventTicketList);
        return "ticket-type/type_list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Principal principal) {
        String userEmail = principal.getName();
        User user = userService.getUserByEmail(userEmail);

        model.addAttribute("ticketType", new TicketType());
        return "ticket-type/type_form";
    }

    @PostMapping("/create")
    public String createTicketType(@RequestParam String name,
                                   @RequestParam BigDecimal price,
                                   @RequestParam int quota,
                                   Principal principal) {
        String userEmail = principal.getName();
        User user = userService.getUserByEmail(userEmail);
        ticketTypeService.create(name, price, quota, user);
        return "redirect:/ticket-types";
    }

    @PostMapping("/delete/{id}")
    public String deleteTicketType(@PathVariable UUID id, Principal principal, RedirectAttributes redirectAttributes) {
        String userEmail = principal.getName();
        User requester = userService.getUserByEmail(principal.getName());

        try {
            ticketTypeService.deleteTicketType(id, requester);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket type deleted successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/ticket-types";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model, Principal principal) {

        // Get the current user
        String userEmail = principal.getName();
        User currentUser = userService.getUserByEmail(userEmail);

        // Fetch and bind the ticket type
        TicketType ticketType = ticketTypeService.getTicketTypeById(id)
                .orElseThrow(() -> new IllegalArgumentException("TicketType not found"));

        model.addAttribute("ticketType", ticketType);
        model.addAttribute("currentUser", currentUser); // optional
        return "ticket-type/type_edit";
    }

    @PostMapping("edit/{id}")
    public String updateTicketType(@PathVariable UUID id,
                                   @ModelAttribute TicketType updatedTicketType,
                                   Principal principal) {
        // Get the current user
        String userEmail = principal.getName();
        User currentUser = userService.getUserByEmail(userEmail);

        // Update the ticket type
        ticketTypeService.updateTicketType(id, updatedTicketType, currentUser);

        return "redirect:/ticket-types";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<TicketType> getTicketType(@PathVariable UUID id) {
        return ticketTypeService.getTicketTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
