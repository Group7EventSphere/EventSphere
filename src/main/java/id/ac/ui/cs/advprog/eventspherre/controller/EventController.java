package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping("/events")
public class EventController {
    private static final Logger logger = Logger.getLogger(EventController.class.getName());

    @Autowired
    private EventManagementService eventManagementService;

    @Autowired
    private UserService userService;

    @Autowired
    private TicketTypeService ticketTypeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String listEvents(Model model) {
        try {
            List<Event> events = eventManagementService.getAllEvents();
            model.addAttribute("events", events != null ? events : new ArrayList<>());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error listing events", e);
            model.addAttribute("events", new ArrayList<>());
            model.addAttribute("errorMessage", "Could not load events.");
        }
        return "events/list";
    }

    @GetMapping("/manage")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String manageEvents(Model model) {
        model.addAttribute("events", eventManagementService.getAllEvents());
        return "events/manage";
    }

    @GetMapping("/{eventId}/edit")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String showEditEventForm(@PathVariable Integer eventId,
                                    Model model,
                                    Principal principal,
                                    RedirectAttributes ra) {
        try {
            Event event = eventManagementService.getEventById(eventId);
            if (event == null) {
                ra.addFlashAttribute("errorMessage", "Event not found.");
                return "redirect:/events/manage";
            }

            User user = userService.getUserByEmail(principal.getName());
            boolean isAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isOrganizer = event.getOrganizerId().equals(user.getId());
            if (!isAdmin && !isOrganizer) {
                ra.addFlashAttribute("errorMessage", "Not authorized to edit.");
                return "redirect:/events/manage";
            }

            EventForm form = new EventForm();
            form.setTitle(event.getTitle());
            form.setDescription(event.getDescription());
            form.setLocation(event.getLocation());
            form.setEventDate(event.getEventDate());
            form.setCapacity(event.getCapacity());
            form.setPublic(event.isPublic());
            // TODO: load existing ticket types if desired

            model.addAttribute("eventForm", form);
            model.addAttribute("eventId", eventId);
            return "events/edit";

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading edit form", e);
            ra.addFlashAttribute("errorMessage", "Failed to load event.");
            return "redirect:/events/manage";
        }
    }

    @PostMapping("/{eventId}/edit")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String updateEvent(@PathVariable Integer eventId,
                              @ModelAttribute EventForm eventForm,
                              Principal principal,
                              RedirectAttributes ra) {
        try {
            // re-check existence & permissions if needed...
            eventManagementService.updateEvent(
                    eventId,
                    eventForm.getTitle(),
                    eventForm.getDescription(),
                    eventForm.getEventDate(),
                    eventForm.getLocation(),
                    eventForm.getCapacity(),
                    eventForm.isPublic()
            );
            ra.addFlashAttribute("successMessage", "Event updated successfully!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating event " + eventId, e);
            ra.addFlashAttribute("errorMessage", "Failed to update event: " + e.getMessage());
        }
        return "redirect:/events/manage";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String showCreateEventForm(Model model) {
        model.addAttribute("eventForm", new EventForm());
        return "events/create";
    }

    @PostMapping("/create")
    public Callable<String> createEvent(@ModelAttribute("eventForm") EventForm eventForm,
                                        Principal principal,
                                        RedirectAttributes ra) {
        return () -> {
            User currentUser = null;
            try {
                currentUser = userService.getUserByEmail(principal.getName());

                Integer organizerId = currentUser.getId();

                // Role check for users who are found (not null)
                if (currentUser.getRole() != User.Role.ORGANIZER && currentUser.getRole() != User.Role.ADMIN) {
                    ra.addFlashAttribute("errorMessage", "You are not authorized to create events.");
                    return "redirect:/events/create";
                }

                CompletableFuture<Event> futureEvent = eventManagementService.createEvent(
                        eventForm.getTitle(),
                        eventForm.getDescription(),
                        eventForm.getEventDate(),
                        eventForm.getLocation(),
                        organizerId
                );

                futureEvent.get();

                ra.addFlashAttribute("successMessage", "Event created successfully!");
                return "redirect:/events/manage";

            } catch (NullPointerException e) {
                logger.log(Level.WARNING, "NullPointerException during event creation by principal " + (principal != null ? principal.getName() : "null") + ": " + e.getMessage(), e);
                ra.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/events/create";
            } catch (InterruptedException | ExecutionException e) {
                Throwable cause = e.getCause();
                String errorMessage = (cause != null && cause.getMessage() != null) ? cause.getMessage() : e.getMessage();
                if (errorMessage == null && cause != null) {
                    errorMessage = "An unexpected error occurred during event creation: " + cause.getClass().getSimpleName();
                } else if (errorMessage == null) {
                    errorMessage = "An unexpected error occurred during event creation: " + e.getClass().getSimple
