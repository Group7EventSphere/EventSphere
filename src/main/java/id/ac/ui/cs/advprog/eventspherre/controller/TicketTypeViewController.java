package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TicketTypeService ticketTypeService;

    @Autowired
    private EventManagementService eventService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listTicketTypes(@PathVariable("eventId") int eventId, Model model) {
        Event event = eventService.getEvent(eventId);
        if (event == null) {
            return "redirect:/events/view";
        }

        List<TicketType> ticketTypes = ticketTypeService.findByEventId(eventId);
        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypes);
        return "ticket-type/manage";
    }

    @GetMapping("/create")
    public String showCreateForm(@PathVariable("eventId") int eventId, Model model, Principal principal) {
        Event event = eventService.getEvent(eventId);
        if (event == null) {
            return "redirect:/events/view";
        }

        String userEmail = principal.getName();
        User user = userService.getUserByEmail(userEmail);

        model.addAttribute("event", event);
        model.addAttribute("ticketType", new TicketType());
        return "ticket-type/type_form";
    }

    @PostMapping("/create")
    public String createTicketType(
            @PathVariable("eventId") int eventId,
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam int quota,
            Principal principal) {

        Event event = eventService.getEvent(eventId);
        if (event == null) {
            return "redirect:/events/view";
        }

        User user = userService.getUserByEmail(principal.getName());
        TicketType ticketType = ticketTypeService.create(name, price, quota, user);

        // Associate ticket type with event
        ticketTypeService.associateWithEvent(ticketType.getId(), eventId);

        return "redirect:/events/" + eventId + "/ticket-types";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable("eventId") int eventId,
            @PathVariable("id") UUID id,
            Model model,
            Principal principal) {

        Event event = eventService.getEvent(eventId);
        if (event == null) {
            return "redirect:/events/view";
        }

        User currentUser = userService.getUserByEmail(principal.getName());
        TicketType ticketType = ticketTypeService.getTicketTypeById(id)
                .orElseThrow(() -> new IllegalArgumentException("TicketType not found"));

        model.addAttribute("event", event);
        model.addAttribute("ticketType", ticketType);
        return "ticket-type/type_form";
    }

    @PostMapping("/edit/{id}")
    public String updateTicketType(
            @PathVariable("eventId") int eventId,
            @PathVariable("id") UUID id,
            @ModelAttribute TicketType updatedTicketType,
            Principal principal) {

        Event event = eventService.getEvent(eventId);
        if (event == null) {
            return "redirect:/events/view";
        }

        User currentUser = userService.getUserByEmail(principal.getName());
        ticketTypeService.updateTicketType(id, updatedTicketType, currentUser);

        return "redirect:/events/" + eventId + "/ticket-types";
    }

    @PostMapping("/delete/{id}")
    public String deleteTicketType(
            @PathVariable("eventId") int eventId,
            @PathVariable("id") UUID id,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User requester = userService.getUserByEmail(principal.getName());

        try {
            ticketTypeService.deleteTicketType(id, requester);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket type deleted successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/events/" + eventId + "/ticket-types";
    }
}
