// src/test/java/id/ac/ui/cs/advprog/eventspherre/service/FileSystemImageStorageServiceTest.java
package id.ac.ui.cs.advprog.eventspherre.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

public class FileSystemImageStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileSystemImageStorageService service;

    @BeforeEach
    void setUp() {
        service = new FileSystemImageStorageService(tempDir);
        service.init();
    }

    @Test
    void storeAndDelete_roundTrip() throws Exception {
        byte[] content = "hello world".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "greeting.png",
                "image/png",
                content
        );

        String storedPath = service.store(file);
        Path stored = Path.of(storedPath);

        assertThat(Files.exists(stored)).isTrue();
        assertThat(Files.readAllBytes(stored)).isEqualTo(content);

        service.delete(storedPath);
        assertThat(Files.exists(stored)).isFalse();
    }

    @Test
    void store_invalidContentType_throws() {
        MockMultipartFile badFile = new MockMultipartFile(
                "file",
                "bad.txt",
                "text/plain",
                new byte[0]
        );
        assertThatThrownBy(() -> service.store(badFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only PNG or JPEG");
    }

    @Test
    void store_tooLarge_throws() {
        byte[] big = new byte[2_000_000];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "big.png",
                "image/png",
                big
        );
        assertThatThrownBy(() -> service.store(largeFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File too large");
    }
}
