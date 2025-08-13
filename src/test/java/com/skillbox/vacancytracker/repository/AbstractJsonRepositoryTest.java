package com.skillbox.vacancytracker.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.skillbox.vacancytracker.exception.RepositoryException;
import com.skillbox.vacancytracker.model.BotUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractJsonRepositoryTest {

    @TempDir
    Path tempDir;

    private TestJsonRepository repository;
    private BotUser testUser;

    @BeforeEach
    void setUp() {
        repository = new TestJsonRepository(tempDir.toString());
        testUser = new BotUser(123L, 456L);
        testUser.setUsername("testuser");
        testUser.setFirstName("John");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir.resolve("test.json"))) {
            Files.delete(tempDir.resolve("test.json"));
        }
    }

    @Test
    void shouldInitializeRepositoryWithEmptyFile() {
        assertThat(tempDir.resolve("test.json")).exists();
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldCreateDirectoryIfNotExists() throws IOException {
        Path newTempDir = tempDir.resolve("subdir");
        Files.deleteIfExists(newTempDir);
        
        TestJsonRepository newRepo = new TestJsonRepository(newTempDir.toString());
        
        assertThat(newTempDir).exists();
        assertThat(newTempDir.resolve("test.json")).exists();
    }

    @Test
    void shouldThrowRepositoryExceptionOnInvalidDirectory() {
        // Use a path that exists but is not a directory (it's a file)
        String invalidPath = tempDir.resolve("test.json").toString();
        
        assertThatThrownBy(() -> new TestJsonRepository(invalidPath))
            .isInstanceOf(RepositoryException.class)
            .hasMessageContaining("Failed to initialize repository");
    }

    @Test
    void shouldSaveAndFindEntity() {
        repository.save(testUser);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(123L);
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldReturnEmptyWhenEntityNotFound() {
        Optional<BotUser> found = repository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckIfEntityExists() {
        assertThat(repository.exists(123L)).isFalse();
        
        repository.save(testUser);
        
        assertThat(repository.exists(123L)).isTrue();
        assertThat(repository.exists(999L)).isFalse();
    }

    @Test
    void shouldUpdateExistingEntity() {
        repository.save(testUser);
        
        testUser.setUsername("updateduser");
        repository.save(testUser);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("updateduser");
    }

    @Test
    void shouldDeleteEntity() {
        repository.save(testUser);
        assertThat(repository.exists(123L)).isTrue();
        
        repository.delete(123L);
        
        assertThat(repository.exists(123L)).isFalse();
        assertThat(repository.findById(123L)).isEmpty();
    }

    @Test
    void shouldDeleteNonExistentEntityWithoutError() {
        repository.delete(999L);
        
        // Should not throw exception
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldFindAllEntities() {
        BotUser user1 = new BotUser(1L, 11L);
        user1.setUsername("user1");
        BotUser user2 = new BotUser(2L, 22L);
        user2.setUsername("user2");
        BotUser user3 = new BotUser(3L, 33L);
        user3.setUsername("user3");
        
        repository.save(user1);
        repository.save(user2);
        repository.save(user3);
        
        List<BotUser> allUsers = repository.findAll();
        assertThat(allUsers).hasSize(3);
        assertThat(allUsers).extracting(BotUser::getUsername)
            .containsExactlyInAnyOrder("user1", "user2", "user3");
    }

    @Test
    void shouldReturnEmptyListWhenNoEntities() {
        List<BotUser> allUsers = repository.findAll();
        assertThat(allUsers).isEmpty();
    }

    @Test
    void shouldPersistDataBetweenInstances() {
        repository.save(testUser);
        
        // Create new repository instance pointing to same file
        TestJsonRepository newRepository = new TestJsonRepository(tempDir.toString());
        
        Optional<BotUser> found = newRepository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        File dataFile = tempDir.resolve("test.json").toFile();
        Files.writeString(dataFile.toPath(), "");
        
        TestJsonRepository newRepository = new TestJsonRepository(tempDir.toString());
        
        assertThat(newRepository.findAll()).isEmpty();
    }

    @Test
    void shouldHandleCorruptedJsonFile() throws IOException {
        File dataFile = tempDir.resolve("test.json").toFile();
        Files.writeString(dataFile.toPath(), "invalid json content");
        
        TestJsonRepository newRepository = new TestJsonRepository(tempDir.toString());
        
        assertThatThrownBy(() -> newRepository.findAll())
            .isInstanceOf(RepositoryException.class)
            .hasMessageContaining("Failed to load data from file");
    }

    @Test
    void shouldHandleReadOnlyFile() throws IOException {
        repository.save(testUser);
        
        File dataFile = tempDir.resolve("test.json").toFile();
        dataFile.setReadOnly();
        
        BotUser newUser = new BotUser(456L, 789L);
        
        try {
            assertThatThrownBy(() -> repository.save(newUser))
                .isInstanceOf(RepositoryException.class)
                .hasMessageContaining("Failed to save data to file");
        } finally {
            dataFile.setWritable(true);
        }
    }

    @Test
    void shouldHandleConcurrentAccess() throws InterruptedException {
        int threadCount = 5;
        int operationsPerThread = 20;
        Thread[] threads = new Thread[threadCount];
        
        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < operationsPerThread; i++) {
                    Long userId = (long) (threadIndex * operationsPerThread + i);
                    BotUser user = new BotUser(userId, userId * 10);
                    user.setUsername("user" + userId);
                    
                    repository.save(user);
                    
                    Optional<BotUser> found = repository.findById(userId);
                    assertThat(found).isPresent();
                    
                    if (i % 2 == 0) {
                        repository.delete(userId);
                        assertThat(repository.exists(userId)).isFalse();
                    }
                }
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Half of the entities should remain (odd indices)
        List<BotUser> remaining = repository.findAll();
        assertThat(remaining).hasSize(threadCount * operationsPerThread / 2);
    }

    @Test
    void shouldHandleNullIdInSave() {
        BotUser userWithoutId = new BotUser();
        userWithoutId.setUsername("noIdUser");
        
        // The actual implementation converts null to "null" string in the map key
        repository.save(userWithoutId);
        
        // Verify it was saved with "null" as the key
        Optional<BotUser> found = repository.findById(null);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("noIdUser");
    }

    @Test
    void shouldHandleNullEntityInSave() {
        assertThatThrownBy(() -> repository.save(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldHandleNullIdInFind() {
        Optional<BotUser> found = repository.findById(null);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldHandleNullIdInExists() {
        assertThat(repository.exists(null)).isFalse();
    }

    @Test
    void shouldHandleNullIdInDelete() {
        repository.delete(null);
        
        // Should not throw exception
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldCreatePrettyPrintedJsonFile() throws IOException {
        repository.save(testUser);
        
        File dataFile = tempDir.resolve("test.json").toFile();
        String fileContent = Files.readString(dataFile.toPath());
        
        // Verify the JSON is pretty-printed (contains newlines and proper structure)
        assertThat(fileContent).contains("\n"); // Contains newlines (not minified)
        assertThat(fileContent).contains("\"123\" : {");
        assertThat(fileContent).contains("\"user_id\" : 123");
        assertThat(fileContent).contains("\"username\" : \"testuser\"");
        // Verify it's properly formatted with indentation
        assertThat(fileContent.split("\n")).hasSizeGreaterThan(5); // Multi-line
    }

    @Test
    void shouldOverwriteEntityWithSameId() {
        BotUser user1 = new BotUser(123L, 456L);
        user1.setUsername("user1");
        user1.setFirstName("First1");
        
        BotUser user2 = new BotUser(123L, 789L);
        user2.setUsername("user2");
        user2.setFirstName("First2");
        
        repository.save(user1);
        repository.save(user2);
        
        List<BotUser> allUsers = repository.findAll();
        assertThat(allUsers).hasSize(1);
        
        BotUser saved = allUsers.get(0);
        assertThat(saved.getUsername()).isEqualTo("user2");
        assertThat(saved.getFirstName()).isEqualTo("First2");
        assertThat(saved.getChatId()).isEqualTo(789L);
    }

    @Test
    void shouldMaintainDataIntegrityAfterMultipleOperations() {
        // Save initial data
        for (int i = 0; i < 10; i++) {
            BotUser user = new BotUser((long) i, (long) (i * 10));
            user.setUsername("user" + i);
            repository.save(user);
        }
        
        assertThat(repository.findAll()).hasSize(10);
        
        // Delete even-indexed users
        for (int i = 0; i < 10; i += 2) {
            repository.delete((long) i);
        }
        
        assertThat(repository.findAll()).hasSize(5);
        
        // Update remaining users
        for (int i = 1; i < 10; i += 2) {
            BotUser user = repository.findById((long) i).orElseThrow();
            user.setUsername("updated_user" + i);
            repository.save(user);
        }
        
        // Verify final state
        List<BotUser> finalUsers = repository.findAll();
        assertThat(finalUsers).hasSize(5);
        assertThat(finalUsers).allMatch(user -> user.getUsername().startsWith("updated_"));
        assertThat(finalUsers).extracting(BotUser::getUserId)
            .containsExactlyInAnyOrder(1L, 3L, 5L, 7L, 9L);
    }

    private static class TestJsonRepository extends AbstractJsonRepository<BotUser, Long> {
        public TestJsonRepository(String dataDirectory) {
            super(dataDirectory, "test.json", new TypeReference<Map<String, BotUser>>() {});
        }

        @Override
        protected Long getId(BotUser entity) {
            return entity.getUserId();
        }
    }
}