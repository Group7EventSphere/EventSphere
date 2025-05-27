// src/test/java/id/ac/ui/cs/advprog/eventspherre/service/ImageStorageServiceTest.java
package id.ac.ui.cs.advprog.eventspherre.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ImageStorageServiceTest {

    @Autowired
    private ImageStorageService storage;

    @Test
    void serviceIsFileBased() {
        assertThat(storage).isInstanceOf(FileSystemImageStorageService.class);
    }
}
