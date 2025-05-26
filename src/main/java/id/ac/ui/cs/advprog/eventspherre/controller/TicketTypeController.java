package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
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

    private final TicketTypeService ticketTypeService;
    private final UserService userService;
    private final EventManagementService eventManagementService;

    public TicketTypeController(TicketTypeService ticketTypeService,
                                UserService userService,
                                EventManagementService eventManagementService) {
        this.ticketTypeService = ticketTypeService;
        this.userService = userService;
        this.eventManagementService = eventManagementService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public String showTicketOverview(Model model, Principal principal) {
        String userEmail = principal.getName();
        userService.getUserByEmail(userEmail);

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
        }        model.addAttribute("eventTicketList", eventTicketList);
        return AppConstants.VIEW_TICKET_TYPE_LIST;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Principal principal) {
        String userEmail = principal.getName();
        User user = userService.getUserByEmail(userEmail);
        boolean isOrganizer = user != null && user.getRole() == User.Role.ORGANIZER;

        List<Event> events = eventManagementService.getAllEvents();
        model.addAttribute("events", events);        model.addAttribute("ticketType", new TicketType());
        model.addAttribute("isGeneralForm", Boolean.TRUE);
        model.addAttribute("isOrganizer", isOrganizer);
        return AppConstants.VIEW_TICKET_TYPE_FORM;
    }

    @PostMapping("/create")
    public String createTicketType(@RequestParam String name,
                                   @RequestParam BigDecimal price,
                                   @RequestParam int quota,
                                   @RequestParam int eventId,
                                   Principal principal) {
        String userEmail = principal.getName();
        User user = userService.getUserByEmail(userEmail);        ticketTypeService.create(name, price, quota, user, eventId);

        return AppConstants.REDIRECT_TICKET_TYPES;
    }

    @PostMapping("/delete/{id}")
    public String deleteTicketType(@PathVariable UUID id, Principal principal, RedirectAttributes redirectAttributes) {
        User requester = userService.getUserByEmail(principal.getName());        try {
            ticketTypeService.deleteTicketType(id, requester);
            redirectAttributes.addFlashAttribute("successMessage", AppConstants.SUCCESS_TICKET_TYPE_DELETED);
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return AppConstants.REDIRECT_TICKET_TYPES;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model, Principal principal) {

        // Get the current user
        String userEmail = principal.getName();
        User currentUser = userService.getUserByEmail(userEmail);        // Fetch and bind the ticket type
        TicketType ticketType = ticketTypeService.getTicketTypeById(id)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ERROR_TICKET_TYPE_NOT_FOUND));

        boolean isOrganizer = currentUser != null && currentUser.getRole() == User.Role.ORGANIZER;
        model.addAttribute("isOrganizer", isOrganizer);        model.addAttribute("ticketType", ticketType);
        model.addAttribute("currentUser", currentUser); // optional
        return AppConstants.VIEW_TICKET_TYPE_EDIT;
    }

    @PostMapping("edit/{id}")
    public String updateTicketType(@PathVariable UUID id,
                                   @ModelAttribute TicketType updatedTicketType,
                                   Principal principal) {
        // Get the current user
        String userEmail = principal.getName();
        User currentUser = userService.getUserByEmail(userEmail);        // Update the ticket type
        ticketTypeService.updateTicketType(id, updatedTicketType, currentUser);

        return AppConstants.REDIRECT_TICKET_TYPES;
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<TicketType> getTicketType(@PathVariable UUID id) {
        return ticketTypeService.getTicketTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
