package id.ac.ui.cs.advprog.eventspherre.constants;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class AppConstantsTest {

    @Test
    void constructor_ShouldThrowUnsupportedOperationException() {
        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            Constructor<AppConstants> constructor = AppConstants.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
        
        // Verify the wrapped exception is UnsupportedOperationException
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }

    @Test
    void httpStatusCodes_ShouldHaveCorrectValues() {
        assertEquals(404, AppConstants.HTTP_STATUS_NOT_FOUND);
        assertEquals(403, AppConstants.HTTP_STATUS_FORBIDDEN);
        assertEquals(500, AppConstants.HTTP_STATUS_INTERNAL_SERVER_ERROR);
    }

    @Test
    void errorMessages_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.ERROR_EVENT_NOT_FOUND);
        assertNotNull(AppConstants.ERROR_USER_NOT_FOUND);
        assertNotNull(AppConstants.ERROR_COULD_NOT_LOAD_EVENTS);
        assertNotNull(AppConstants.ERROR_COULD_NOT_LOAD_EVENT_DETAILS);
        assertNotNull(AppConstants.ERROR_FAILED_TO_LOAD_EVENT);
        assertNotNull(AppConstants.ERROR_FAILED_TO_UPDATE_EVENT);
        assertNotNull(AppConstants.ERROR_FAILED_TO_DELETE_EVENT);
        assertNotNull(AppConstants.ERROR_FAILED_TO_TOGGLE_VISIBILITY);
        assertNotNull(AppConstants.ERROR_COULD_NOT_CREATE_EVENT);
        assertNotNull(AppConstants.ERROR_NOT_AUTHORIZED_CREATE);
        assertNotNull(AppConstants.ERROR_NOT_AUTHORIZED_DELETE);
        assertNotNull(AppConstants.ERROR_ACCESS_DENIED);
        assertNotNull(AppConstants.ERROR_PAGE_NOT_FOUND);
        assertNotNull(AppConstants.ERROR_INTERNAL_SERVER);
        assertNotNull(AppConstants.ERROR_UNEXPECTED);

        assertFalse(AppConstants.ERROR_EVENT_NOT_FOUND.isEmpty());
        assertFalse(AppConstants.ERROR_USER_NOT_FOUND.isEmpty());
        assertFalse(AppConstants.ERROR_ACCESS_DENIED.isEmpty());
    }

    @Test
    void successMessages_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.SUCCESS_EVENT_CREATED);
        assertNotNull(AppConstants.SUCCESS_EVENT_UPDATED);
        assertNotNull(AppConstants.SUCCESS_EVENT_DELETED);
        assertNotNull(AppConstants.SUCCESS_VISIBILITY_CHANGED);

        assertFalse(AppConstants.SUCCESS_EVENT_CREATED.isEmpty());
        assertFalse(AppConstants.SUCCESS_EVENT_UPDATED.isEmpty());
        assertFalse(AppConstants.SUCCESS_EVENT_DELETED.isEmpty());
        assertFalse(AppConstants.SUCCESS_VISIBILITY_CHANGED.isEmpty());
    }

    @Test
    void jwtErrorMessages_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.JWT_ERROR_UNAUTHORIZED);
        assertNotNull(AppConstants.JWT_ERROR_EXPIRED);
        assertNotNull(AppConstants.JWT_ERROR_MALFORMED);
        assertNotNull(AppConstants.JWT_ERROR_UNSUPPORTED);
        assertNotNull(AppConstants.JWT_ERROR_INVALID_SIGNATURE);
        assertNotNull(AppConstants.JWT_ERROR_BAD_REQUEST);

        assertFalse(AppConstants.JWT_ERROR_UNAUTHORIZED.isEmpty());
        assertFalse(AppConstants.JWT_ERROR_EXPIRED.isEmpty());
        assertFalse(AppConstants.JWT_ERROR_MALFORMED.isEmpty());
    }

    @Test
    void viewNames_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.VIEW_ERROR);
        assertNotNull(AppConstants.VIEW_UNAUTHORIZED);
        assertNotNull(AppConstants.VIEW_EVENTS_LIST);
        assertNotNull(AppConstants.VIEW_EVENTS_MANAGE);
        assertNotNull(AppConstants.VIEW_EVENTS_CREATE);
        assertNotNull(AppConstants.VIEW_EVENTS_EDIT);
        assertNotNull(AppConstants.VIEW_EVENTS_DETAIL);

        assertFalse(AppConstants.VIEW_ERROR.isEmpty());
        assertFalse(AppConstants.VIEW_UNAUTHORIZED.isEmpty());
        assertFalse(AppConstants.VIEW_EVENTS_LIST.isEmpty());
    }

    @Test
    void redirectUrls_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.REDIRECT_EVENTS);
        assertNotNull(AppConstants.REDIRECT_EVENTS_MANAGE);
        assertNotNull(AppConstants.REDIRECT_EVENTS_CREATE);

        assertFalse(AppConstants.REDIRECT_EVENTS.isEmpty());
        assertFalse(AppConstants.REDIRECT_EVENTS_MANAGE.isEmpty());
        assertFalse(AppConstants.REDIRECT_EVENTS_CREATE.isEmpty());

        assertTrue(AppConstants.REDIRECT_EVENTS.startsWith("redirect:"));
        assertTrue(AppConstants.REDIRECT_EVENTS_MANAGE.startsWith("redirect:"));
        assertTrue(AppConstants.REDIRECT_EVENTS_CREATE.startsWith("redirect:"));
    }

    @Test
    void visibilityStatus_ShouldHaveCorrectValues() {
        assertEquals("public", AppConstants.VISIBILITY_PUBLIC);
        assertEquals("private", AppConstants.VISIBILITY_PRIVATE);
    }

    @Test
    void adminControllerConstants_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.ERROR_INVALID_ROLE_FILTER);
        assertNotNull(AppConstants.ERROR_INVALID_ROLE_ALL_USERS);
        assertNotNull(AppConstants.ERROR_CANNOT_DELETE_OWN_ACCOUNT);
        assertNotNull(AppConstants.SUCCESS_USER_UPDATED);
        assertNotNull(AppConstants.SUCCESS_USER_CREATED);
        assertNotNull(AppConstants.SUCCESS_USER_DELETED);
        assertNotNull(AppConstants.VIEW_ADMIN_USER_MANAGEMENT);
        assertNotNull(AppConstants.REDIRECT_ADMIN_USERS);

        assertFalse(AppConstants.ERROR_INVALID_ROLE_FILTER.isEmpty());
        assertFalse(AppConstants.SUCCESS_USER_UPDATED.isEmpty());
        assertTrue(AppConstants.REDIRECT_ADMIN_USERS.startsWith("redirect:"));
    }

    @Test
    void profileControllerConstants_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.ERROR_UPDATING_PROFILE);
        assertNotNull(AppConstants.ERROR_CHANGING_PASSWORD);
        assertNotNull(AppConstants.ERROR_CURRENT_PASSWORD_INCORRECT);
        assertNotNull(AppConstants.ERROR_NEW_PASSWORDS_NO_MATCH);
        assertNotNull(AppConstants.SUCCESS_PROFILE_UPDATED);
        assertNotNull(AppConstants.SUCCESS_PASSWORD_CHANGED);
        assertNotNull(AppConstants.VIEW_PROFILE);
        assertNotNull(AppConstants.REDIRECT_PROFILE);

        assertFalse(AppConstants.ERROR_UPDATING_PROFILE.isEmpty());
        assertFalse(AppConstants.SUCCESS_PROFILE_UPDATED.isEmpty());
        assertTrue(AppConstants.REDIRECT_PROFILE.startsWith("redirect:"));
    }

    @Test
    void ticketConstants_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.ERROR_TICKET_TYPE_NOT_FOUND);
        assertNotNull(AppConstants.SUCCESS_TICKET_TYPE_DELETED);
        assertNotNull(AppConstants.VIEW_TICKET_TYPE_LIST);
        assertNotNull(AppConstants.VIEW_TICKET_TYPE_FORM);
        assertNotNull(AppConstants.VIEW_TICKET_TYPE_EDIT);
        assertNotNull(AppConstants.REDIRECT_TICKET_TYPES);

        assertFalse(AppConstants.ERROR_TICKET_TYPE_NOT_FOUND.isEmpty());
        assertFalse(AppConstants.SUCCESS_TICKET_TYPE_DELETED.isEmpty());
        assertTrue(AppConstants.REDIRECT_TICKET_TYPES.startsWith("redirect:"));
    }

    @Test
    void serviceErrorMessages_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.ERROR_TICKET_TYPE_NOT_FOUND_SERVICE);
        assertNotNull(AppConstants.ERROR_NOT_ENOUGH_TICKETS);
        assertNotNull(AppConstants.ERROR_ATTENDEE_MUST_BE_SPECIFIED);
        assertNotNull(AppConstants.ERROR_TICKET_NOT_FOUND_SERVICE);
        assertNotNull(AppConstants.ERROR_INVALID_ROLE);
        assertNotNull(AppConstants.ERROR_USER_NOT_FOUND_WITH_EMAIL);

        assertFalse(AppConstants.ERROR_TICKET_TYPE_NOT_FOUND_SERVICE.isEmpty());
        assertFalse(AppConstants.ERROR_NOT_ENOUGH_TICKETS.isEmpty());
        assertFalse(AppConstants.ERROR_ATTENDEE_MUST_BE_SPECIFIED.isEmpty());
    }

    @Test
    void validationConstants_ShouldHaveCorrectValues() {
        assertEquals(1, AppConstants.MIN_RATING);
        assertEquals(5, AppConstants.MAX_RATING);
        assertEquals(0, AppConstants.MIN_VALID_ID);
        assertEquals(0, AppConstants.DEFAULT_USER_ID);
        assertEquals(8, AppConstants.CONFIRMATION_CODE_LENGTH);
        assertEquals(500L, AppConstants.ASYNC_PROCESSING_DELAY);
    }

    @Test
    void configurationConstants_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.TICKET_CODE_PREFIX);
        assertFalse(AppConstants.TICKET_CODE_PREFIX.isEmpty());
        assertEquals("TKT-", AppConstants.TICKET_CODE_PREFIX);
    }

    @Test
    void jwtConstants_ShouldHaveCorrectValues() {
        assertEquals(3600L, AppConstants.JWT_EXPIRATION_TIME);
    }

    @Test
    void userConstants_ShouldHaveCorrectValues() {
        assertEquals(0.0, AppConstants.DEFAULT_BALANCE);
        assertEquals(6, AppConstants.MIN_PASSWORD_LENGTH);
    }

    @Test
    void testConstants_ShouldHaveCorrectValues() {
        assertEquals("VIP", AppConstants.TEST_VIP_TICKET_TYPE);
        assertEquals("Regular", AppConstants.TEST_REGULAR_TICKET_TYPE);
        assertEquals("120.00", AppConstants.TEST_PRICE_120);
        assertEquals("100.00", AppConstants.TEST_PRICE_100);
        assertEquals("150.00", AppConstants.TEST_PRICE_150);
        assertEquals(5, AppConstants.TEST_QUOTA_5);
        assertEquals(10, AppConstants.TEST_QUOTA_10);
        assertEquals(25, AppConstants.TEST_QUOTA_25);
        assertEquals(50, AppConstants.TEST_QUOTA_50);
        assertEquals(1, AppConstants.TEST_REDUCE_QUANTITY_1);
        assertEquals(2, AppConstants.TEST_REDUCE_QUANTITY_2);
        assertEquals(4, AppConstants.TEST_REDUCE_QUANTITY_4);
        assertEquals(1, AppConstants.TEST_USER_ID_1);

        assertNotNull(AppConstants.TEST_ERROR_CANNOT_DELETE);
        assertFalse(AppConstants.TEST_ERROR_CANNOT_DELETE.isEmpty());
    }

    @Test
    void statusConstants_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.STATUS_SOFT_DELETED);
        assertFalse(AppConstants.STATUS_SOFT_DELETED.isEmpty());
        assertEquals("SOFT_DELETED", AppConstants.STATUS_SOFT_DELETED);
    }

    @Test
    void logMessages_ShouldNotBeNullOrEmpty() {
        assertNotNull(AppConstants.LOG_ERROR_UPDATING_EVENT);
        assertNotNull(AppConstants.LOG_ERROR_CREATING_EVENT_BY_PRINCIPAL);
        assertNotNull(AppConstants.LOG_WARN_NPE_EVENT_CREATION);
        assertNotNull(AppConstants.LOG_ERROR_LOADING_EVENT_DETAILS);
        assertNotNull(AppConstants.LOG_ERROR_DELETING_EVENT);
        assertNotNull(AppConstants.LOG_ERROR_TOGGLING_VISIBILITY);
        assertNotNull(AppConstants.LOG_JWT_TOKEN_EXPIRED);
        assertNotNull(AppConstants.LOG_UNSUPPORTED_JWT_TOKEN);
        assertNotNull(AppConstants.LOG_MALFORMED_JWT_TOKEN);

        assertFalse(AppConstants.LOG_ERROR_UPDATING_EVENT.isEmpty());
        assertFalse(AppConstants.LOG_JWT_TOKEN_EXPIRED.isEmpty());
        assertFalse(AppConstants.LOG_MALFORMED_JWT_TOKEN.isEmpty());
    }

    @Test
    void nullPrincipal_ShouldHaveCorrectValue() {
        assertEquals("null", AppConstants.NULL_PRINCIPAL);
    }
}
