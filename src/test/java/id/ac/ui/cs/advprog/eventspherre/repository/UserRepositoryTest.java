package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// auto-configure an embedded test database (H2) for you
@AutoConfigureTestDatabase
// point JPA at your repo package
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
// point JPA at your entity package
@EntityScan(basePackageClasses = User.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    
    private User admin;
    private User organizer;
    private User attendee1;
    private User attendee2;
    
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        
        // Create test users with different roles
        admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setPassword("adminpass");
        admin.setRole(User.Role.ADMIN);
        
        organizer = new User();
        organizer.setName("Event Organizer");
        organizer.setEmail("organizer@events.com");
        organizer.setPassword("orgpass");
        organizer.setRole(User.Role.ORGANIZER);
        
        attendee1 = new User();
        attendee1.setName("John Doe");
        attendee1.setEmail("john@example.com");
        attendee1.setPassword("johnpass");
        attendee1.setRole(User.Role.ATTENDEE);
        
        attendee2 = new User();
        attendee2.setName("Jane Smith");
        attendee2.setEmail("jane@example.com");
        attendee2.setPassword("janepass");
        attendee2.setRole(User.Role.ATTENDEE);
        
        userRepository.saveAll(List.of(admin, organizer, attendee1, attendee2));
    }

    @Test
    @DisplayName("When saving a User, findByEmail should return it")
    void shouldSaveAndFindByEmail() {
        // given
        User user = new User();
        user.setName("Alice Wonderland");
        user.setEmail("alice@example.com");
        user.setPassword("s3cr3t");

        // when
        User saved = userRepository.save(user);

        // then
        Optional<User> found = userRepository.findByEmail("alice@example.com");
        assertThat(found)
            .isPresent()
            .get()
              .extracting(User::getId, User::getName, User::getEmail, User::getPassword)
              .containsExactly(
                  saved.getId(),
                  "Alice Wonderland",
                  "alice@example.com",
                  "s3cr3t"
              );
    }

    @Test
    @DisplayName("findByEmail for non-existent email returns empty Optional")
    void whenEmailNotFound_thenReturnEmpty() {
        Optional<User> result = userRepository.findByEmail("nobody@nowhere.com");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("CRUD operations: create, read, update, delete")
    void testCrudOperations() {
        // — Create —
        User user = new User();
        user.setName("Bob Builder");
        user.setEmail("bob@builder.com");
        user.setPassword("canwefixit");
        User created = userRepository.save(user);

        // — Read —
        Optional<User> readBack = userRepository.findById(created.getId());
        assertThat(readBack)
            .isPresent()
            .get()
              .extracting(User::getEmail)
              .isEqualTo("bob@builder.com");

        // — Update —
        User toUpdate = readBack.get();
        toUpdate.setName("Bob The Builder");
        userRepository.save(toUpdate);
        Optional<User> updated = userRepository.findById(created.getId());
        assertThat(updated)
            .isPresent()
            .get()
              .extracting(User::getName)
              .isEqualTo("Bob The Builder");

        // — Delete —
        userRepository.delete(updated.get());
        assertThat(userRepository.findById(created.getId())).isEmpty();
    }
    
    @Test
    @DisplayName("findByRole should return users with the specific role")
    void testFindByRole() {
        // Test finding admins
        List<User> admins = userRepository.findByRole(User.Role.ADMIN);
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getEmail()).isEqualTo("admin@example.com");
        
        // Test finding organizers
        List<User> organizers = userRepository.findByRole(User.Role.ORGANIZER);
        assertThat(organizers).hasSize(1);
        assertThat(organizers.get(0).getEmail()).isEqualTo("organizer@events.com");
        
        // Test finding attendees
        List<User> attendees = userRepository.findByRole(User.Role.ATTENDEE);
        assertThat(attendees).hasSize(2);
        assertThat(attendees).extracting(User::getEmail)
            .containsExactlyInAnyOrder("john@example.com", "jane@example.com");
    }
    
    @Test
    @DisplayName("findByNameOrEmailContainingIgnoreCase should return users matching name or email pattern")
    void testFindByNameOrEmailContainingIgnoreCase() {
        // Test searching by partial name
        List<User> usersWithDoe = userRepository.findByNameOrEmailContainingIgnoreCase("Doe");
        assertThat(usersWithDoe).hasSize(1);
        assertThat(usersWithDoe.get(0).getName()).isEqualTo("John Doe");
        
        // Test searching by partial email
        List<User> usersWithExampleDomain = userRepository.findByNameOrEmailContainingIgnoreCase("example.com");
        assertThat(usersWithExampleDomain).hasSize(3);
        assertThat(usersWithExampleDomain).extracting(User::getEmail)
            .containsExactlyInAnyOrder("admin@example.com", "john@example.com", "jane@example.com");
        
        // Test case insensitivity
        List<User> usersWithAdminLowercase = userRepository.findByNameOrEmailContainingIgnoreCase("admin");
        List<User> usersWithAdminUppercase = userRepository.findByNameOrEmailContainingIgnoreCase("ADMIN");
        assertThat(usersWithAdminLowercase).hasSize(1);
        assertThat(usersWithAdminUppercase).hasSize(1);
        assertThat(usersWithAdminLowercase.get(0).getId()).isEqualTo(usersWithAdminUppercase.get(0).getId());
    }
    
    @Test
    @DisplayName("findByRoleAndNameOrEmailContainingIgnoreCase should filter by role and search term")
    void testFindByRoleAndNameOrEmailContainingIgnoreCase() {
        // Test filtering admins with search term
        List<User> adminsWithExample = userRepository.findByRoleAndNameOrEmailContainingIgnoreCase(
            User.Role.ADMIN, "example");
        assertThat(adminsWithExample).hasSize(1);
        assertThat(adminsWithExample.get(0).getEmail()).isEqualTo("admin@example.com");
        
        // Test filtering attendees with search term
        List<User> attendeesWithJohn = userRepository.findByRoleAndNameOrEmailContainingIgnoreCase(
            User.Role.ATTENDEE, "john");
        assertThat(attendeesWithJohn).hasSize(1);
        assertThat(attendeesWithJohn.get(0).getName()).isEqualTo("John Doe");
        
        // Test with no matches
        List<User> organizersWithJane = userRepository.findByRoleAndNameOrEmailContainingIgnoreCase(
            User.Role.ORGANIZER, "jane");
        assertThat(organizersWithJane).isEmpty();
    }
}
