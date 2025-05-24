import java.util.HashMap;
import java.util.Map;

public class debug_test {
    public static void main(String[] args) {
        // Recreate the exact scenario from the failing test
        Event event = new Event();
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("isPublic", null);
        event.setDetails(detailsMap);
        
        System.out.println("Event created with details map containing isPublic: null");
        System.out.println("Details map: " + event.getDetails());
        System.out.println("isPublic field: " + event.isPublic);
        
        try {
            boolean result = event.isPublic();
            System.out.println("isPublic() returned: " + result + " (no exception thrown)");
        } catch (NullPointerException e) {
            System.out.println("NPE thrown as expected: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Other exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}

// Simplified Event class to match the failing scenario
class Event {
    private Boolean isPublic;
    private Map<String, Object> details;
    
    public Event() {}
    
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public boolean isPublic() {
        if (isPublic != null) {
            return isPublic;
        }

        // For backward compatibility, try to get from details map
        if (details != null && details.containsKey("isPublic")) {
            Object isPublicObj = details.get("isPublic");
            if (isPublicObj instanceof Boolean) {
                this.isPublic = (Boolean) isPublicObj; // Cache it in the field
                return this.isPublic;
            } else if (isPublicObj == null) {
                // This should throw NPE for consistency with the test expectation
                throw new NullPointerException("isPublic value in details map is null");
            }
        }
        return false; // Default value when not set
    }
}
