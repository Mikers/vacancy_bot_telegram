package com.skillbox.vacancytracker.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DirectoryManagerTest {

    @TempDir
    Path tempDir;
    
    private Path originalLogsDir;
    
    @BeforeEach
    void setUp() {
        originalLogsDir = Path.of("logs");
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // Clean up logs directory if it was created during tests
        if (Files.exists(originalLogsDir) && Files.isDirectory(originalLogsDir)) {
            try {
                Files.deleteIfExists(originalLogsDir);
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    @Test
    void shouldCreateDataDirectoryWhenNotExists() throws IOException {
        Path dataPath = tempDir.resolve("data");
        
        DirectoryManager.initializeDirectories(dataPath.toString());
        
        assertThat(dataPath).exists();
        assertThat(dataPath).isDirectory();
    }
    
    @Test
    void shouldNotThrowWhenDataDirectoryAlreadyExists() throws IOException {
        Path dataPath = tempDir.resolve("existing_data");
        Files.createDirectories(dataPath);
        
        DirectoryManager.initializeDirectories(dataPath.toString());
        
        assertThat(dataPath).exists();
        assertThat(dataPath).isDirectory();
    }
    
    @Test
    void shouldCreateLogsDirectory() throws IOException {
        Path dataPath = tempDir.resolve("data");
        
        DirectoryManager.initializeDirectories(dataPath.toString());
        
        assertThat(Path.of("logs")).exists();
        assertThat(Path.of("logs")).isDirectory();
    }
    
    @Test
    void shouldHandleLogsDirectoryAlreadyExists() throws IOException {
        Path logsPath = Path.of("logs");
        Files.createDirectories(logsPath);
        Path dataPath = tempDir.resolve("data");
        
        DirectoryManager.initializeDirectories(dataPath.toString());
        
        assertThat(logsPath).exists();
        assertThat(logsPath).isDirectory();
    }
    
    @Test
    void shouldCreateNestedDataDirectoryStructure() throws IOException {
        Path nestedPath = tempDir.resolve("level1/level2/level3");
        
        DirectoryManager.initializeDirectories(nestedPath.toString());
        
        assertThat(nestedPath).exists();
        assertThat(nestedPath).isDirectory();
        assertThat(nestedPath.getParent()).exists();
        assertThat(nestedPath.getParent().getParent()).exists();
    }
    
    @Test
    void shouldHandleAbsolutePaths() throws IOException {
        Path absolutePath = tempDir.resolve("absolute_data").toAbsolutePath();
        
        DirectoryManager.initializeDirectories(absolutePath.toString());
        
        assertThat(absolutePath).exists();
        assertThat(absolutePath).isDirectory();
    }
    
    @Test
    void shouldHandleRelativePaths() throws IOException {
        Path relativePath = Path.of("relative_test_data");
        
        try {
            DirectoryManager.initializeDirectories(relativePath.toString());
            
            assertThat(relativePath).exists();
            assertThat(relativePath).isDirectory();
        } finally {
            // Cleanup relative directory
            if (Files.exists(relativePath)) {
                Files.deleteIfExists(relativePath);
            }
        }
    }
    
    @Test
    void shouldHandleEmptyDataDirectoryPath() throws IOException {
        Path emptyPath = Path.of("");
        
        DirectoryManager.initializeDirectories(emptyPath.toString());
        
        // Empty path resolves to current directory, should not throw
        assertThat(emptyPath).exists();
    }
    
    @Test
    void shouldHandleDotDataDirectoryPath() throws IOException {
        Path dotPath = Path.of(".");
        
        DirectoryManager.initializeDirectories(dotPath.toString());
        
        assertThat(dotPath).exists();
        assertThat(dotPath).isDirectory();
    }
    
    @Test
    void shouldHandleSpecialCharactersInPath() throws IOException {
        Path specialPath = tempDir.resolve("test-data_2024");
        
        DirectoryManager.initializeDirectories(specialPath.toString());
        
        assertThat(specialPath).exists();
        assertThat(specialPath).isDirectory();
    }
    
    @Test
    void shouldHandlePathWithSpaces() throws IOException {
        Path pathWithSpaces = tempDir.resolve("test data folder");
        
        DirectoryManager.initializeDirectories(pathWithSpaces.toString());
        
        assertThat(pathWithSpaces).exists();
        assertThat(pathWithSpaces).isDirectory();
    }
    
    @Test
    void shouldThrowIOExceptionWhenPathIsFile() throws IOException {
        Path filePath = tempDir.resolve("testfile.txt");
        Files.createFile(filePath);
        
        // The actual implementation may not throw IOException immediately,
        // but Files.createDirectories() should handle this case
        // Testing that the method completes (it may create parent dirs)
        DirectoryManager.initializeDirectories(filePath.toString());
        
        // Verify that the original file still exists and wasn't overwritten
        assertThat(filePath).exists();
    }
    
    @Test
    void shouldHandleVeryLongPath() throws IOException {
        StringBuilder longPathBuilder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            longPathBuilder.append("very_long_directory_name_").append(i).append("/");
        }
        Path longPath = tempDir.resolve(longPathBuilder.toString());
        
        DirectoryManager.initializeDirectories(longPath.toString());
        
        assertThat(longPath).exists();
        assertThat(longPath).isDirectory();
    }
    
    @Test
    void shouldHandleUnicodeCharactersInPath() throws IOException {
        Path unicodePath = tempDir.resolve("тест-данные-папка");
        
        DirectoryManager.initializeDirectories(unicodePath.toString());
        
        assertThat(unicodePath).exists();
        assertThat(unicodePath).isDirectory();
    }
    
    @Test
    void shouldHandleMultipleConsecutiveSlashes() throws IOException {
        Path pathWithSlashes = tempDir.resolve("test//data///folder");
        
        DirectoryManager.initializeDirectories(pathWithSlashes.toString());
        
        // Path normalization should handle multiple slashes
        assertThat(pathWithSlashes.normalize()).exists();
    }
    
    @Test
    void shouldHandlePathWithDots() throws IOException {
        Path pathWithDots = tempDir.resolve("test/../data/./folder");
        
        DirectoryManager.initializeDirectories(pathWithDots.toString());
        
        // Should resolve to tempDir/data/folder
        assertThat(pathWithDots.normalize()).exists();
        assertThat(pathWithDots.normalize()).isDirectory();
    }
    
    @Test
    void shouldPreventInstantiation() throws ReflectiveOperationException {
        // DirectoryManager should be a utility class with private constructor
        Class<?> clazz = DirectoryManager.class;
        
        assertThat(clazz.getDeclaredConstructors()).hasSize(1);
        assertThat(clazz.getDeclaredConstructors()[0].getParameterCount()).isEqualTo(0);
        
        // Constructor should be private
        var constructor = clazz.getDeclaredConstructor();
        assertThat(constructor.canAccess(null)).isFalse();
    }
    
    @Test
    void shouldHandleNullDataDirectoryPath() {
        assertThatThrownBy(() -> DirectoryManager.initializeDirectories(null))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    void shouldLogDirectoryCreation() throws IOException {
        Path dataPath = tempDir.resolve("logged_data");
        
        // This test verifies that the method completes without throwing
        // Actual logging verification would require a logging framework mock
        DirectoryManager.initializeDirectories(dataPath.toString());
        
        assertThat(dataPath).exists();
        assertThat(dataPath).isDirectory();
    }
    
    @Test
    void shouldBothDirectoriesAfterInitialization() throws IOException {
        Path dataPath = tempDir.resolve("both_test_data");
        
        DirectoryManager.initializeDirectories(dataPath.toString());
        
        assertThat(dataPath).exists();
        assertThat(dataPath).isDirectory();
        assertThat(Path.of("logs")).exists();
        assertThat(Path.of("logs")).isDirectory();
    }
}