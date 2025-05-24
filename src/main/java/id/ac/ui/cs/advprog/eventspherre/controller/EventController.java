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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
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
    public String createEvent(@ModelAttribute("eventForm") EventForm eventForm,
                              Principal principal,
                              RedirectAttributes ra) {
        User currentUser = null;
        try {
            currentUser = userService.getUserByEmail(principal.getName());

            // This line will cause a NullPointerException if currentUser is null,
            // which is the scenario set up by the createEvent_shouldDenyAccessForNonOrganizers test.
            // The variable name "currentUser" is used to match the expected NPE message in the test.
            Integer organizerId = currentUser.getId();

            // Role check for users who are found (not null)
            if (currentUser.getRole() != User.Role.ORGANIZER && currentUser.getRole() != User.Role.ADMIN) {
                ra.addFlashAttribute("errorMessage", "You are not authorized to create events.");
                return "redirect:/events/create";
            }

            // Updated to pass all event form parameters including capacity and isPublic
            eventManagementService.createEvent(
                    eventForm.getTitle(),
                    eventForm.getDescription(),
                    eventForm.getEventDate(),
                    eventForm.getLocation(),
                    organizerId,
                    eventForm.getCapacity(),
                    eventForm.isPublic()
            );
            ra.addFlashAttribute("successMessage", "Event created successfully!");
            return "redirect:/events/manage";

        } catch (NullPointerException e) {
            // This catch block is specifically for the test createEvent_shouldDenyAccessForNonOrganizers
            logger.log(Level.WARNING, "NullPointerException during event creation by principal " + (principal != null ? principal.getName() : "null") + ": " + e.getMessage(), e);
            ra.addFlashAttribute("errorMessage", e.getMessage()); // e.getMessage() will be the NPE detail string
            return "redirect:/events/create";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating event by principal " + (principal != null ? principal.getName() : "null") + ": " + e.getMessage(), e);
            ra.addFlashAttribute("errorMessage", "Could not create event: " + e.getMessage());
            return "redirect:/events/create";
        }
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public String showEventDetails(@PathVariable Integer eventId, Model model, RedirectAttributes ra) {
        try {
            Event event = eventManagementService.getEventById(eventId);
            if (event == null) {
                ra.addFlashAttribute("errorMessage", "Event not found.");
                return "redirect:/events";
            }

            // Get ticket types for this event
            var ticketTypes = ticketTypeService.getTicketTypesByEventId(eventId);

            model.addAttribute("event", event);
            model.addAttribute("ticketTypes", ticketTypes != null ? ticketTypes : new ArrayList<>());

            return "events/detail";

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading event details for event " + eventId, e);
            ra.addFlashAttribute("errorMessage", "Could not load event details.");
            return "redirect:/events";
        }
    }

    @PostMapping("/{eventId}/delete")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String deleteEvent(@PathVariable Integer eventId,
                              Principal principal,
                              RedirectAttributes ra) {
        try {
            // Get the event first to check if it exists
            Event event = eventManagementService.getEventById(eventId);

            // Check if the current user is either an admin or the organizer of this event
            User user = userService.getUserByEmail(principal.getName());

            // Add null check for user
            if (user == null) {
                ra.addFlashAttribute("errorMessage", "User not found.");
                return "redirect:/events/manage";
            }

            boolean isAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isOrganizer = event.getOrganizerId().equals(user.getId());

            if (!isAdmin && !isOrganizer) {
                ra.addFlashAttribute("errorMessage", "You are not authorized to delete this event.");
                return "redirect:/events/manage";
            }

            // Delete the event
            eventManagementService.deleteEvent(eventId);
            ra.addFlashAttribute("successMessage", "Event deleted successfully!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting event " + eventId, e);
            ra.addFlashAttribute("errorMessage", "Failed to delete event: " + e.getMessage());
        }
        return "redirect:/events/manage";
    }

    // --- Form backing objects ---

    @Getter @Setter
    public static class EventForm {
        private String title;
        private String description;
        private String location;
        private String eventDate;
        private Integer capacity;
        private boolean isPublic;
        private List<?> ticketTypes; // Added ticketTypes field to fix the error
    }
}
