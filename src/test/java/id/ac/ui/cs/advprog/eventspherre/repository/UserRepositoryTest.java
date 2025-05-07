package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
}
