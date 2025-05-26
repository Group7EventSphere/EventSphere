package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/events/{eventId}/ticket-types")
public class TicketTypeViewController {

    private final TicketTypeService ticketTypeService;
    private final EventManagementService eventService;
    private final UserService userService;

    public TicketTypeViewController(TicketTypeService ticketTypeService, 
                                   EventManagementService eventService, 
                                   UserService userService) {
        this.ticketTypeService = ticketTypeService;
        this.eventService = eventService;
        this.userService = userService;
    }    @GetMapping
    public String listTicketTypes(@PathVariable("eventId") int eventId, Model model) {
        Event event = eventService.getEvent(eventId);
        if (event == null) {
            return AppConstants.REDIRECT_EVENTS_VIEW;
        }

        List<TicketType> ticketTypes = ticketTypeService.findByEventId(eventId);
        model.addAttribute(AppConstants.ATTR_EVENT, event);
        model.addAttribute(AppConstants.ATTR_TICKET_TYPES, ticketTypes);
        return AppConstants.VIEW_TICKET_TYPE_MANAGE;
    }    @GetMapping("/create")
    public String showCreateForm(@PathVariable("eventId") int eventId, Model model, Principal principal) {
        Event event = eventService.getEvent(eventId);
        if (event == null) {
            return AppConstants.REDIRECT_EVENTS_VIEW;
        }

        String userEmail = principal.getName();
        User user = userService.getUserByEmail(userEmail);
        boolean isOrganizer = user != null && user.getRole() == User.Role.ORGANIZER;

        model.addAttribute(AppConstants.ATTR_EVENT, event);
        model.addAttribute(AppConstants.ATTR_TICKET_TYPE, new TicketType());
        model.addAttribute(AppConstants.ATTR_IS_GENERAL_FORM, false);
        model.addAttribute(AppConstants.ATTR_IS_ORGANIZER, isOrganizer);
        return AppConstants.VIEW_TICKET_TYPE_FORM;
    }    @PostMapping("/create")
    public String createTicketType(
            @PathVariable("eventId") int eventId,
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam int quota,
            Principal principal) {

        Event event = eventService.getEvent(eventId);
        if (event == null) {
            return AppConstants.REDIRECT_EVENTS_VIEW;
        }

        User user = userService.getUserByEmail(principal.getName());
        TicketType ticketType = ticketTypeService.create(name, price, quota, user, eventId);

        // Associate ticket type with event
        ticketTypeService.associateWithEvent(ticketType.getId(), eventId);

        return AppConstants.REDIRECT_EVENTS_PREFIX + eventId + AppConstants.TICKET_TYPES_SUFFIX;
    }    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable("eventId") int eventId,
            @PathVariable("id") UUID id,
            Model model,
            Principal principal) {

        Event event = eventService.getEvent(eventId);
        if (event == null) {
            return AppConstants.REDIRECT_EVENTS_VIEW;
        }

        User currentUser = userService.getUserByEmail(principal.getName());
        TicketType ticketType = ticketTypeService.getTicketTypeById(id)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ERROR_TICKET_TYPE_NOT_FOUND));

        boolean isOrganizer = currentUser != null && currentUser.getRole() == User.Role.ORGANIZER;
        model.addAttribute(AppConstants.ATTR_IS_ORGANIZER, isOrganizer);

        model.addAttribute(AppConstants.ATTR_EVENT, event);
        model.addAttribute(AppConstants.ATTR_TICKET_TYPE, ticketType);
        return AppConstants.VIEW_TICKET_TYPE_EDIT;
    }    @PostMapping("/edit/{id}")
    public String updateTicketType(
            @PathVariable("eventId") int eventId,
            @PathVariable("id") UUID id,
            @ModelAttribute TicketType updatedTicketType,
            Principal principal) {

        Event event = eventService.getEvent(eventId);
        if (event == null) {
            return AppConstants.REDIRECT_EVENTS_VIEW;
        }

        User currentUser = userService.getUserByEmail(principal.getName());
        ticketTypeService.updateTicketType(id, updatedTicketType, currentUser);

        return AppConstants.REDIRECT_EVENTS_PREFIX + eventId + AppConstants.TICKET_TYPES_SUFFIX;
    }    @PostMapping("/delete/{id}")
    public String deleteTicketType(
            @PathVariable("eventId") int eventId,
            @PathVariable("id") UUID id,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User requester = userService.getUserByEmail(principal.getName());

        try {
            ticketTypeService.deleteTicketType(id, requester);
            redirectAttributes.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE, AppConstants.SUCCESS_TICKET_TYPE_DELETED);
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE, e.getMessage());
        }

        return AppConstants.REDIRECT_EVENTS_PREFIX + eventId + AppConstants.TICKET_TYPES_SUFFIX;
    }
}
