package id.ac.ui.cs.advprog.eventspherre;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EventSpherreApplicationTests {

    @Test
    void contextLoads() {
        // This test simply checks if the application context loads successfully.
        // No additional assertions are needed as the test will fail if the context cannot be loaded.
    }

    @Test
    void testMainMethod() {
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            String[] args = {"arg1", "arg2"};
            
            EventSpherreApplication.main(args);
            
            mockedSpringApplication.verify(() -> SpringApplication.run(EventSpherreApplication.class, args));
        }
    }

}
