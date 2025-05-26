package id.ac.ui.cs.advprog.eventspherre.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileSystemImageStorageServiceTest {

    @TempDir
    Path tempRoot;

    private FileSystemImageStorageService service;

    @BeforeEach
    void setUp() {
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
        assertArrayEquals(content,
                Files.readAllBytes(savedPath),
                "Content must match");
    }

    @Test
    void store_validJpeg_shouldSaveFile() {
        byte[] content = "jpeg data".getBytes();
        MockMultipartFile jpg = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", content
        );

        String saved = service.store(jpg);
        assertTrue(Files.exists(Path.of(saved)),
                "JPEG store must create the file");
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
    void delete_existingFile_shouldRemoveIt() {
        MockMultipartFile png = new MockMultipartFile(
                "file", "toDelete.png", "image/png", "data".getBytes()
        );
        String path = service.store(png);
        Path saved = Path.of(path);
        assertTrue(Files.exists(saved), "File should exist before delete()");

        service.delete(path);
        assertFalse(Files.exists(saved), "File should be gone after delete()");
    }

    @Test
    void delete_nonExistingFile_shouldNotThrow() {
        assertDoesNotThrow(() ->
                service.delete(tempRoot.resolve("no-such-file.png").toString())
        );
    }

    // ===== New tests to cover IOException paths =====

    @Test
    void init_rootIsAFile_shouldWrapIOException() throws IOException {
        // make a file at the path instead of a directory
        Path fileInsteadOfDir = Files.createTempFile("not-a-dir", ".txt");
        FileSystemImageStorageService svc = new FileSystemImageStorageService(fileInsteadOfDir);

        RuntimeException ex = assertThrows(RuntimeException.class, svc::init);
        assertTrue(ex.getMessage().contains("Could not create storage directory"));
        assertTrue(ex.getCause() instanceof IOException);
    }

    @Test
    void store_whenInputStreamFails_shouldWrapIOException() throws IOException {
        // re-init service
        service = new FileSystemImageStorageService(tempRoot);
        service.init();

        // mock a multipart file whose getInputStream() throws
        MultipartFile broken = mock(MultipartFile.class);
        when(broken.getContentType()).thenReturn("image/png");
        when(broken.isEmpty()).thenReturn(false);
        when(broken.getSize()).thenReturn(123L);
        when(broken.getOriginalFilename()).thenReturn("bad.png");
        when(broken.getInputStream()).thenThrow(new IOException("boom"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.store(broken)
        );
        assertTrue(ex.getMessage().contains("Failed to store image"));
        assertTrue(ex.getCause() instanceof IOException);
    }
}
