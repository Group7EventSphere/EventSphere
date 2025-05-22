package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.TicketTypeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.service.EventManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
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

    // Handle the /events path used in the navbar for all authenticated users
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String listEvents(Model model) {
        try {
            List<Event> events = eventManagementService.getAllEvents();
            // If events is null, provide an empty list instead
            if (events == null) {
                events = new ArrayList<>();
                logger.warning("Events list is null, using empty list instead");
            }
            model.addAttribute("events", events);
            return "events/list";
        } catch (Exception e) {
            logger.severe("Error in listEvents: " + e.getMessage());
            model.addAttribute("errorMessage", "An error occurred while loading events. Please try again later.");
            model.addAttribute("events", new ArrayList<>());
            return "events/list";
        }
    }

    // Handle the /events/manage path for organizers and admins
    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public String manageEvents(Model model, Principal principal) {
        try {
            List<Event> events = eventManagementService.getAllEvents();
            model.addAttribute("events", events != null ? events : new ArrayList<>());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to load events: " + e.getMessage());
            model.addAttribute("events", new ArrayList<>());
        }
        return "events/manage";
    }

    // Show the event creation form
    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public String showCreateEventForm(Model model) {
        // Create a new empty form for the template
        EventForm eventForm = new EventForm();
        eventForm.setTitle("");
        eventForm.setDescription("");
        eventForm.setLocation("");
        eventForm.setCapacity(100); // Default capacity
        eventForm.setPublic(true);  // Default to public
        eventForm.setTicketTypes(new ArrayList<>());

        model.addAttribute("eventForm", eventForm);
        return "events/create";
    }

    // Process the event creation form submission
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public String createEvent(@ModelAttribute EventForm eventForm,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            // Get the current user
            User currentUser = userService.getUserByEmail(principal.getName());

            // Create the event using the service with individual parameters
            Event savedEvent = eventManagementService.createEvent(
                    eventForm.getTitle(),
                    eventForm.getDescription(),
                    eventForm.getEventDate(),    // Corrected: Pass eventDate from form
                    eventForm.getLocation(),     // Corrected: Pass location from form
                    currentUser.getId()          // Corrected: Pass organizerId from current user
            );

            // Then process and save each ticket type
            if (eventForm.getTicketTypes() != null) {
                for (TicketTypeForm ticketTypeForm : eventForm.getTicketTypes()) {
                    ticketTypeService.create(
                            ticketTypeForm.getName(),
                            ticketTypeForm.getPrice(),
                            ticketTypeForm.getAvailableSeats(),
                            currentUser
                    );
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
            return "redirect:/events/manage";
        } catch (Exception e) {
            logger.severe("Error creating event: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create event: " + e.getMessage());
            return "redirect:/events/create";
        }
    }

    // Inner class to use as form backing object
    public static class EventForm {
        private String title;
        private String description;
        private String location;
        private String eventDate;
        private Integer capacity;
        private boolean isPublic;
        private List<TicketTypeForm> ticketTypes;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getEventDate() { return eventDate; }
        public void setEventDate(String eventDate) { this.eventDate = eventDate; }

        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }

        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

        public List<TicketTypeForm> getTicketTypes() { return ticketTypes; }
        public void setTicketTypes(List<TicketTypeForm> ticketTypes) { this.ticketTypes = ticketTypes; }
    }

    public static class TicketTypeForm {
        private String name;
        private Double price;
        private Integer availableSeats;
        private String description;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getPrice() {
            // Convert Double to BigDecimal when needed
            return price != null ? new BigDecimal(price.toString()) : null;
        }
        public void setPrice(Double price) { this.price = price; }

        public Integer getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
