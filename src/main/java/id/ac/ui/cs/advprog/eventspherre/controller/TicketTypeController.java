package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ticket-types")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("ticketType", new TicketType());
        return "ticket-type/create";
    }

    @PostMapping
    public String createTicketType(@ModelAttribute TicketType ticketType, @SessionAttribute("loggedInUser") User user) {
        ticketTypeService.create(ticketType.getName(), ticketType.getPrice(), ticketType.getQuota(), user);
        return "redirect:/ticket-types";
    }

    @GetMapping
    public String listTicketTypes(Model model) {
        model.addAttribute("ticketTypes", ticketTypeService.findAll());
        return "ticket-type/list";
    }
}

