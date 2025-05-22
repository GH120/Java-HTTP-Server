package com.example.core.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WebRootHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void testValidWebRootConstructor() {
        assertDoesNotThrow(() -> new WebRootHandler(tempDir.toString()));
    }

    @Test
    void testNonExistentWebRootConstructor() {
        String nonExistentPath = tempDir.resolve("nonexistent").toString();
        Exception exception = assertThrows(WebRootNotFoundException.class, () -> {
            new WebRootHandler(nonExistentPath);
        });

        assertEquals("Webroot provided does not exist", exception.getMessage());
    }

    @Test
    void testWebRootIsNotDirectoryConstructor() throws Exception {
        File tempFile = File.createTempFile("notADir", ".tmp");
        tempFile.deleteOnExit();

        Exception exception = assertThrows(WebRootNotFoundException.class, () -> {
            new WebRootHandler(tempFile.getAbsolutePath());
        });

        assertEquals("Webroot provided does not exist", exception.getMessage());
    }

    @Test
    void testCheckIfEndsWithSlashReturnsTrueForValidFileInsideWebRoot() throws Exception {
        // Arrange
        File file = new File(tempDir.toFile(), "index.html");
        assertTrue(file.createNewFile());

        WebRootHandler handler = new WebRootHandler(tempDir.toString());

        // Act
        boolean result = handler.CheckIfEndsWithSlash("index.html");

        // Assert
        assertTrue(result);
    }

    @Test
    void testCheckIfEndsWithSlashReturnsFalseForNonExistentFile() throws Exception {
        WebRootHandler handler = new WebRootHandler(tempDir.toString());

        boolean result = handler.CheckIfEndsWithSlash("no_such_file.txt");

        assertFalse(result);
    }

    @Test
    void testCheckIfEndsWithSlashReturnsFalseForPathTraversal() throws Exception {
        File outsideFile = File.createTempFile("outside", ".txt");
        outsideFile.deleteOnExit();

        WebRootHandler handler = new WebRootHandler(tempDir.toString());

        // Tentativa de escapar do webRoot
        String traversalPath = "../../" + outsideFile.getName();

        boolean result = handler.CheckIfEndsWithSlash(traversalPath);

        assertFalse(result);
    }
}
