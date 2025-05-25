package id.ac.ui.cs.advprog.eventspherre.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemImageStorageServiceTest {

    @TempDir
    Path tempRoot;

    private FileSystemImageStorageService service;

    @BeforeEach
    void setUp() {
        // point the service at our temp directory
        service = new FileSystemImageStorageService(tempRoot);
        service.init();
    }

    @Test
    void init_shouldCreateRootDirectory() {
        assertTrue(Files.exists(tempRoot), "Root directory must exist after init()");
        assertTrue(Files.isDirectory(tempRoot), "Root path must be a directory");
    }

    @Test
    void store_validPng_shouldSaveFile() throws IOException {
        byte[] content = "dummy image".getBytes();
        MockMultipartFile png = new MockMultipartFile(
                "file", "pic.png", "image/png", content
        );

        String saved = service.store(png);

        Path savedPath = Path.of(saved);
        assertTrue(Files.exists(savedPath), "Stored file must exist on disk");
        assertArrayEquals(content, Files.readAllBytes(savedPath), "Content must match");
    }

    @Test
    void store_validJpeg_shouldSaveFile() throws IOException {
        byte[] content = "jpeg data".getBytes();
        MockMultipartFile jpg = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", content
        );

        String saved = service.store(jpg);

        assertTrue(Files.exists(Path.of(saved)));
    }

    @Test
    void store_emptyFile_shouldThrow() {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.store(empty)
        );
        assertEquals("Cannot upload empty file", ex.getMessage());
    }

    @Test
    void store_invalidContentType_shouldThrow() {
        MockMultipartFile txt = new MockMultipartFile(
                "file", "note.txt", "text/plain", "hello".getBytes()
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.store(txt)
        );
        assertEquals("Only PNG or JPEG images are allowed", ex.getMessage());
    }

    @Test
    void store_tooLargeFile_shouldThrow() {
        byte[] big = new byte[1_000_001]; // just over 1MB
        MockMultipartFile largePng = new MockMultipartFile(
                "file", "big.png", "image/png", big
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.store(largePng)
        );
        assertEquals("File too large", ex.getMessage());
    }

    @Test
    void delete_existingFile_shouldRemoveIt() throws IOException {
        // first store a file
        MockMultipartFile png = new MockMultipartFile(
                "file", "toDelete.png", "image/png", "data".getBytes()
        );
        String path = service.store(png);
        Path saved = Path.of(path);
        assertTrue(Files.exists(saved));

        // now delete
        service.delete(path);
        assertFalse(Files.exists(saved), "File should be gone after delete()");
    }

    @Test
    void delete_nonExistingFile_shouldNotThrow() {
        assertDoesNotThrow(() -> service.delete(
                tempRoot.resolve("no-such-file.png").toString()
        ));
    }
}
