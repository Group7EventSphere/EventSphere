package id.ac.ui.cs.advprog.eventspherre.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class User {

    private Integer id;
    private String name;
    private String email;
    private String password;
    private Date createdAt;
    private Date updatedAt;
    private Role role;

    public enum Role {
        ADMIN, ORGANIZER, ATTENDEE
    }
}
