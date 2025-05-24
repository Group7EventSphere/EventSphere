package id.ac.ui.cs.advprog.eventspherre.constants;

/**
 * Application constants to replace magic numbers and hardcoded strings.
 * This improves code maintainability and reduces SonarCloud code smells.
 */
public final class AppConstants {
    
    // Private constructor to prevent instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // HTTP Status Codes
    public static final int HTTP_STATUS_NOT_FOUND = 404;
    public static final int HTTP_STATUS_FORBIDDEN = 403;
    public static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;
    
    // Error Messages
    public static final String ERROR_EVENT_NOT_FOUND = "Event not found.";
    public static final String ERROR_USER_NOT_FOUND = "User not found.";
    public static final String ERROR_COULD_NOT_LOAD_EVENTS = "Could not load events.";
    public static final String ERROR_COULD_NOT_LOAD_EVENT_DETAILS = "Could not load event details.";
    public static final String ERROR_FAILED_TO_LOAD_EVENT = "Failed to load event.";
    public static final String ERROR_FAILED_TO_UPDATE_EVENT = "Failed to update event: ";
    public static final String ERROR_FAILED_TO_DELETE_EVENT = "Failed to delete event: ";
    public static final String ERROR_FAILED_TO_TOGGLE_VISIBILITY = "Failed to toggle event visibility: ";
    public static final String ERROR_COULD_NOT_CREATE_EVENT = "Could not create event: ";
    public static final String ERROR_NOT_AUTHORIZED_CREATE = "You are not authorized to create events.";
    public static final String ERROR_NOT_AUTHORIZED_DELETE = "You are not authorized to delete this event.";
    public static final String ERROR_ACCESS_DENIED = "Access denied";
    public static final String ERROR_PAGE_NOT_FOUND = "Page not found";
    public static final String ERROR_INTERNAL_SERVER = "Internal server error";
    public static final String ERROR_UNEXPECTED = "An unexpected error occurred";
    
    // Success Messages
    public static final String SUCCESS_EVENT_CREATED = "Event created successfully!";
    public static final String SUCCESS_EVENT_UPDATED = "Event updated successfully!";
    public static final String SUCCESS_EVENT_DELETED = "Event deleted successfully!";
    public static final String SUCCESS_VISIBILITY_CHANGED = "Event visibility changed to ";
    
    // JWT Error Messages
    public static final String JWT_ERROR_UNAUTHORIZED = "Unauthorized";
    public static final String JWT_ERROR_EXPIRED = "JWT token has expired";
    public static final String JWT_ERROR_MALFORMED = "Malformed JWT token";
    public static final String JWT_ERROR_UNSUPPORTED = "Unsupported JWT token";
    public static final String JWT_ERROR_INVALID_SIGNATURE = "Invalid JWT signature";
    public static final String JWT_ERROR_BAD_REQUEST = "Bad Request";
    
    // View Names
    public static final String VIEW_ERROR = "error";
    public static final String VIEW_UNAUTHORIZED = "unauthorized";
    public static final String VIEW_EVENTS_LIST = "events/list";
    public static final String VIEW_EVENTS_MANAGE = "events/manage";
    public static final String VIEW_EVENTS_CREATE = "events/create";
    public static final String VIEW_EVENTS_EDIT = "events/edit";
    public static final String VIEW_EVENTS_DETAIL = "events/detail";
    
    // Redirect URLs
    public static final String REDIRECT_EVENTS = "redirect:/events";
    public static final String REDIRECT_EVENTS_MANAGE = "redirect:/events/manage";
    public static final String REDIRECT_EVENTS_CREATE = "redirect:/events/create";
      // Visibility Status
    public static final String VISIBILITY_PUBLIC = "public";
    public static final String VISIBILITY_PRIVATE = "private";
    
    // Admin Controller Constants
    public static final String ERROR_INVALID_ROLE_FILTER = "Invalid role filter. Showing search results only.";
    public static final String ERROR_INVALID_ROLE_ALL_USERS = "Invalid role filter. Showing all users.";
    public static final String ERROR_CANNOT_DELETE_OWN_ACCOUNT = "You cannot delete your own account";
    public static final String ERROR_UPDATING_USER = "Error updating user: ";
    public static final String ERROR_UPDATING_PASSWORD = "Error updating password: ";
    public static final String ERROR_CREATING_USER = "Error creating user: ";
    public static final String ERROR_DELETING_USER = "Error deleting user: ";
    public static final String SUCCESS_USER_UPDATED = "User updated successfully";
    public static final String SUCCESS_USER_UPDATED_NO_ROLE = "User updated successfully. Note: You cannot change your own role as an admin.";
    public static final String SUCCESS_PASSWORD_UPDATED = "Password updated successfully";
    public static final String SUCCESS_USER_CREATED = "User created successfully";
    public static final String SUCCESS_USER_DELETED = "User deleted successfully";
    public static final String VIEW_ADMIN_USER_MANAGEMENT = "admin/user-management";
    public static final String REDIRECT_ADMIN_USERS = "redirect:/admin/users";
    
    // Profile Controller Constants
    public static final String ERROR_UPDATING_PROFILE = "Error updating profile: ";
    public static final String ERROR_CHANGING_PASSWORD = "Error changing password: ";
    public static final String ERROR_CURRENT_PASSWORD_INCORRECT = "Current password is incorrect";
    public static final String ERROR_NEW_PASSWORDS_NO_MATCH = "New passwords do not match";
    public static final String SUCCESS_PROFILE_UPDATED = "Profile updated successfully";
    public static final String SUCCESS_PASSWORD_CHANGED = "Password changed successfully";
    public static final String VIEW_PROFILE = "profile";
    public static final String REDIRECT_PROFILE = "redirect:/profile";
    
    // Ticket Type Controller Constants
    public static final String ERROR_TICKET_TYPE_NOT_FOUND = "TicketType not found";
    public static final String SUCCESS_TICKET_TYPE_DELETED = "Ticket type deleted successfully.";
    public static final String VIEW_TICKET_TYPE_LIST = "ticket-type/type_list";
    public static final String VIEW_TICKET_TYPE_FORM = "ticket-type/type_form";
    public static final String VIEW_TICKET_TYPE_EDIT = "ticket-type/type_edit";
    public static final String REDIRECT_TICKET_TYPES = "redirect:/ticket-types";
    
    // Ticket Controller Constants
    public static final String ERROR_INVALID_TICKET_TYPE_ID = "Invalid ticket type ID";
    public static final String ERROR_INSUFFICIENT_BALANCE = "Insufficient balance. Please top up your account.";
    public static final String SUCCESS_TICKET_PURCHASED = "Successfully purchased %d ticket(s).";
    public static final String VIEW_TICKET_SELECT = "ticket/select";
    public static final String VIEW_TICKET_CREATE = "ticket/create";
    public static final String VIEW_TICKET_DETAIL = "ticket/detail";
    public static final String VIEW_TICKET_LIST = "ticket/list";    public static final String REDIRECT_TICKETS = "redirect:/tickets";
    public static final String REDIRECT_TICKETS_CREATE = "redirect:/tickets/create";
    
    // Service Error Messages
    public static final String ERROR_TICKET_TYPE_NOT_FOUND_SERVICE = "Ticket type not found";
    public static final String ERROR_NOT_ENOUGH_TICKETS = "Not enough tickets left";
    public static final String ERROR_ATTENDEE_MUST_BE_SPECIFIED = "Attendee must be specified with a valid user ID";
    public static final String ERROR_TICKET_NOT_FOUND_SERVICE = "Ticket not found";
    public static final String ERROR_INVALID_ROLE = "Invalid role: ";
    public static final String ERROR_USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";
    public static final String ERROR_ONLY_ADMINS_DELETE_TICKET_TYPES = "Only admins can delete ticket types";
    public static final String ERROR_CANNOT_DELETE_TICKET_TYPE_WITH_TICKETS = "Cannot delete ticket type with existing tickets.";
    public static final String ERROR_INVALID_REVIEW = "Invalid review: Review text cannot be empty and rating must be between 1 and 5.";
    public static final String ERROR_REVIEW_NOT_FOUND = "Review not found";
    public static final String ERROR_ALREADY_SUBMITTED_REVIEW = "You have already submitted a review for this event.";
    public static final String LOG_JWT_TOKEN_EXPIRED = "JWT token expired: {}";
    public static final String LOG_UNSUPPORTED_JWT_TOKEN = "Unsupported JWT token: {}";
    public static final String LOG_MALFORMED_JWT_TOKEN = "Malformed JWT token: {}";
      // Transaction Status
    public static final String STATUS_SOFT_DELETED = "SOFT_DELETED";
    
    // Log Messages
    public static final String LOG_ERROR_UPDATING_EVENT = "Error updating event {}";
    public static final String LOG_ERROR_CREATING_EVENT_BY_PRINCIPAL = "Error creating event by principal {}: {}";
    public static final String LOG_WARN_NPE_EVENT_CREATION = "NullPointerException during event creation by principal {}: {}";
    public static final String LOG_ERROR_LOADING_EVENT_DETAILS = "Error loading event details for event {}";
    public static final String LOG_ERROR_DELETING_EVENT = "Error deleting event {}";
    public static final String LOG_ERROR_TOGGLING_VISIBILITY = "Error toggling visibility for event {}";
      // Other constants
    public static final String NULL_PRINCIPAL = "null";
    
    // Validation Constants
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MIN_VALID_ID = 0;
    public static final int DEFAULT_USER_ID = 0;
    public static final int CONFIRMATION_CODE_LENGTH = 8;
    public static final long ASYNC_PROCESSING_DELAY = 500L;
      // Configuration Constants
    public static final String TICKET_CODE_PREFIX = "TKT-";
    
    // JWT Constants
    public static final long JWT_EXPIRATION_TIME = 3600L; // 1 hour
      // User Constants
    public static final double DEFAULT_BALANCE = 0.0;
    public static final int MIN_PASSWORD_LENGTH = 6;
    
    // Test Constants
    public static final String TEST_VIP_TICKET_TYPE = "VIP";
    public static final String TEST_REGULAR_TICKET_TYPE = "Regular";
    public static final String TEST_PRICE_120 = "120.00";
    public static final String TEST_PRICE_100 = "100.00";
    public static final String TEST_PRICE_150 = "150.00";
    public static final int TEST_QUOTA_5 = 5;
    public static final int TEST_QUOTA_10 = 10;
    public static final int TEST_QUOTA_25 = 25;
    public static final int TEST_QUOTA_50 = 50;
    public static final int TEST_REDUCE_QUANTITY_1 = 1;
    public static final int TEST_REDUCE_QUANTITY_2 = 2;
    public static final int TEST_REDUCE_QUANTITY_4 = 4;
    public static final int TEST_USER_ID_1 = 1;
    public static final String TEST_ERROR_CANNOT_DELETE = "Cannot delete ticket type";
}
