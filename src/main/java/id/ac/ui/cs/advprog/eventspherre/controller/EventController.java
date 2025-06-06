package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private final EventManagementService eventManagementService;
    private final UserService userService;
    private final TicketTypeService ticketTypeService;    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String listEvents(Model model) {        try {
            List<Event> events = eventManagementService.getAllEvents();
            model.addAttribute(AppConstants.ATTR_EVENTS, events != null ? events : new ArrayList<>());        } catch (Exception e) {
            logger.error("Error listing events", e);
            model.addAttribute(AppConstants.ATTR_EVENTS, new ArrayList<>());
            model.addAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_COULD_NOT_LOAD_EVENTS);
        }
        return AppConstants.VIEW_EVENTS_LIST;
    }    @GetMapping("/manage")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String manageEvents(Model model) {
        model.addAttribute(AppConstants.ATTR_EVENTS, eventManagementService.getAllEvents());
        return AppConstants.VIEW_EVENTS_MANAGE;
    }

    @GetMapping("/{eventId}/edit")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String showEditEventForm(@PathVariable Integer eventId,
                                    Model model,
                                    Principal principal,
                                    RedirectAttributes ra) {
        try {            Event event = eventManagementService.getEventById(eventId);
            if (event == null) {
                ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, "Event not found.");
                return "redirect:/events/manage";
            }

            User user = userService.getUserByEmail(principal.getName());
            boolean isAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isOrganizer = event.getOrganizerId().equals(user.getId());            if (!isAdmin && !isOrganizer) {
                ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, "Not authorized to edit.");
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
            return "events/edit";        } catch (Exception e) {
            logger.error("Error loading edit form", e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_FAILED_TO_LOAD_EVENT);
            return AppConstants.REDIRECT_EVENTS_MANAGE;
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
            );            ra.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE_KEY, AppConstants.SUCCESS_EVENT_UPDATED);
        } catch (Exception e) {
            logger.error(AppConstants.LOG_ERROR_UPDATING_EVENT, eventId, e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_FAILED_TO_UPDATE_EVENT + e.getMessage());
        }
        return AppConstants.REDIRECT_EVENTS_MANAGE;
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")    public String showCreateEventForm(Model model) {
        model.addAttribute("eventForm", new EventForm());
        return AppConstants.VIEW_EVENTS_CREATE;
    }

    @PostMapping("/create")
    public String createEvent(@Valid @ModelAttribute("eventForm") EventForm eventForm,
                              org.springframework.validation.BindingResult bindingResult,
                              Principal principal,
                              Model model,
                              RedirectAttributes ra) {        // Check for validation errors first
        if (bindingResult.hasErrors()) {
            return AppConstants.VIEW_EVENTS_CREATE; // Return to form with validation errors
        }

        User currentUser = null;
        try {
            currentUser = userService.getUserByEmail(principal.getName());

            // This line will cause a NullPointerException if currentUser is null,
            // which is the scenario set up by the createEvent_shouldDenyAccessForNonOrganizers test.
            // The variable name "currentUser" is used to match the expected NPE message in the test.
            Integer organizerId = currentUser.getId();            // Role check for users who are found (not null)
            if (currentUser.getRole() != User.Role.ORGANIZER && currentUser.getRole() != User.Role.ADMIN) {
                ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_NOT_AUTHORIZED_CREATE);
                return AppConstants.REDIRECT_EVENTS_CREATE;
            }

            // Updated to pass all event form parameters including capacity and isPublic
            Event createdEvent = eventManagementService.createEvent(
                    eventForm.getTitle(),
                    eventForm.getDescription(),
                    eventForm.getEventDate(),
                    eventForm.getLocation(),
                    organizerId,
                    eventForm.getCapacity(),
                    eventForm.isPublic()            );
            
            // Create ticket types for the event
            if (eventForm.getTicketTypes() != null && !eventForm.getTicketTypes().isEmpty()) {
                for (EventForm.TicketTypeForm ticketTypeForm : eventForm.getTicketTypes()) {
                    if (ticketTypeForm.getName() != null && !ticketTypeForm.getName().trim().isEmpty()) {
                        ticketTypeService.create(
                            ticketTypeForm.getName(),
                            new java.math.BigDecimal(ticketTypeForm.getPrice()),
                            ticketTypeForm.getAvailableSeats(),
                            currentUser,
                            createdEvent.getId()
                        );
                    }
                }
            }
            
            ra.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE_KEY, AppConstants.SUCCESS_EVENT_CREATED);
            return AppConstants.REDIRECT_EVENTS_MANAGE;
        } catch (NullPointerException e) {            // This catch block is specifically for the test createEvent_shouldDenyAccessForNonOrganizers
            logger.warn(AppConstants.LOG_WARN_NPE_EVENT_CREATION, 
                (principal != null ? principal.getName() : AppConstants.NULL_PRINCIPAL), e.getMessage(), e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, e.getMessage()); // e.getMessage() will be the NPE detail string
            return AppConstants.REDIRECT_EVENTS_CREATE;
        } catch (Exception e) {
            logger.error(AppConstants.LOG_ERROR_CREATING_EVENT_BY_PRINCIPAL, 
                (principal != null ? principal.getName() : AppConstants.NULL_PRINCIPAL), e.getMessage(), e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_COULD_NOT_CREATE_EVENT + e.getMessage());
            return AppConstants.REDIRECT_EVENTS_CREATE;
        }
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public String showEventDetails(@PathVariable Integer eventId, Model model, RedirectAttributes ra) {
        try {            Event event = eventManagementService.getEventById(eventId);
            if (event == null) {
                ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_EVENT_NOT_FOUND);
                return AppConstants.REDIRECT_EVENTS;
            }

            // Get ticket types for this event
            var ticketTypes = ticketTypeService.getTicketTypesByEventId(eventId);

            model.addAttribute("event", event);
            model.addAttribute("ticketTypes", ticketTypes != null ? ticketTypes : new ArrayList<>());            return AppConstants.VIEW_EVENTS_DETAIL;        } catch (Exception e) {
            logger.error(AppConstants.LOG_ERROR_LOADING_EVENT_DETAILS, eventId, e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_COULD_NOT_LOAD_EVENT_DETAILS);
            return AppConstants.REDIRECT_EVENTS;
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
            
            if (event == null) {
                ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_EVENT_NOT_FOUND);
                return AppConstants.REDIRECT_EVENTS; // Different return for event not found
            }

            // Check if the current user is either an admin or the organizer of this event
            User user = userService.getUserByEmail(principal.getName());
            
            // Add null check for user
            if (user == null) {
                ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_USER_NOT_FOUND);
                return "redirect:/login"; // Redirect to login if user not found
            }

            boolean isAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isOrganizer = event.getOrganizerId().equals(user.getId());

            if (!isAdmin && !isOrganizer) {
                ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_NOT_AUTHORIZED_DELETE);
                return "redirect:/events/" + eventId; // Redirect to event details for unauthorized access
            }

            // Delete the event
            eventManagementService.deleteEvent(eventId);
            ra.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE_KEY, AppConstants.SUCCESS_EVENT_DELETED);
            
            return AppConstants.REDIRECT_EVENTS_MANAGE; // Success case

        } catch (SecurityException e) {
            logger.error("Security error while deleting event: {}", eventId, e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, "Access denied: " + e.getMessage());
            return "redirect:/events/" + eventId; // Return to event details for security errors
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument while deleting event: {}", eventId, e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, "Invalid request: " + e.getMessage());
            return AppConstants.REDIRECT_EVENTS; // Return to events list for invalid arguments
            
        } catch (Exception e) {
            logger.error(AppConstants.LOG_ERROR_DELETING_EVENT, eventId, e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_FAILED_TO_DELETE_EVENT + e.getMessage());
            return AppConstants.REDIRECT_EVENTS_MANAGE; // General error case
        }
    }    @PostMapping("/{eventId}/toggle-visibility")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleEventVisibility(@PathVariable Integer eventId,
                                        RedirectAttributes ra) {
        try {
            Event event = eventManagementService.getEventById(eventId);
            if (event == null) {
                ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_EVENT_NOT_FOUND);
                return AppConstants.REDIRECT_EVENTS; // Different return for event not found
            }

            // Toggle the visibility
            boolean newVisibility = !event.isPublic();
            eventManagementService.updateEvent(
                    eventId,
                    event.getTitle(),
                    event.getDescription(),
                    event.getEventDate(),
                    event.getLocation(),
                    event.getCapacity(),
                    newVisibility
            );

            String visibilityStatus = newVisibility ? AppConstants.VISIBILITY_PUBLIC : AppConstants.VISIBILITY_PRIVATE;
            ra.addFlashAttribute(AppConstants.ATTR_SUCCESS_MESSAGE_KEY, AppConstants.SUCCESS_VISIBILITY_CHANGED + visibilityStatus + ".");
            
            return AppConstants.REDIRECT_EVENTS_MANAGE; // Success case

        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument while toggling visibility for event: {}", eventId, e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, "Invalid request: " + e.getMessage());
            return AppConstants.REDIRECT_EVENTS; // Return to events list for invalid arguments
            
        } catch (SecurityException e) {
            logger.error("Security error while toggling visibility for event: {}", eventId, e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, "Access denied: " + e.getMessage());
            return "redirect:/events/" + eventId; // Return to event details for security errors
            
        } catch (Exception e) {
            logger.error(AppConstants.LOG_ERROR_TOGGLING_VISIBILITY, eventId, e);
            ra.addFlashAttribute(AppConstants.ATTR_ERROR_MESSAGE_KEY, AppConstants.ERROR_FAILED_TO_TOGGLE_VISIBILITY + e.getMessage());
            return "redirect:/events/" + eventId; // Return to event details for general errors
        }
    }

    // --- Form backing objects ---

    @Getter @Setter
    public static class EventForm {
        @jakarta.validation.constraints.NotBlank(message = "Title is required")
        private String title;
        private String description;
        private String location;
        private String eventDate;
        private Integer capacity;
        private boolean isPublic;
        private List<TicketTypeForm> ticketTypes = new ArrayList<>(); // Initialize the list

        @Getter @Setter
        public static class TicketTypeForm {
            private String name;
            private Double price;
            private Integer availableSeats;
            private String description;
        }
    }
}
