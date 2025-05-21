package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.TicketType;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/ticket-types")
public class TicketTypeController {

    @Autowired
    private final TicketTypeService ticketTypeService;

    @Autowired
    private UserService userService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @GetMapping
    public String listTicketTypes(Model model, Principal principal) {
        String userEmail = principal.getName();
        User user = userService.getUserByEmail(userEmail);

        List<TicketType> ticketTypes = ticketTypeService.findAll();
        model.addAttribute("ticketTypes", ticketTypes);
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
    public String deleteTicketType(@PathVariable UUID id, Principal principal) {
        String userEmail = principal.getName();
        User user = userService.getUserByEmail(userEmail);

        ticketTypeService.deleteTicketType(id, user);
        return "redirect:/ticket-types";
    }
}
