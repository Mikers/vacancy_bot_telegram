package com.skillbox.vacancytracker.repository;

import com.skillbox.vacancytracker.model.UserVacancy;
import com.skillbox.vacancytracker.model.Vacancy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JsonUserVacancyRepositoryTest {

    @TempDir
    Path tempDir;

    private JsonUserVacancyRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JsonUserVacancyRepository(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir.resolve("user_vacancies"))) {
            Files.delete(tempDir.resolve("user_vacancies"));
        }
    }

    @Test
    void shouldCreateRepositoryWithUserVacanciesFile() {
        assertThat(tempDir.resolve("user_vacancies")).exists();
    }

    @Test
    void shouldSaveAndRetrieveUserVacancy() {
        UserVacancy userVacancy = createTestUserVacancy(123L, "test-vacancy");
        
        repository.save(userVacancy);
        
        Optional<UserVacancy> found = repository.findById(userVacancy.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(123L);
        assertThat(found.get().getVacancyId()).isEqualTo("test-vacancy");
        assertThat(found.get().isNew()).isTrue();
    }

    @Test
    void shouldFindUserVacanciesByUserId() {
        UserVacancy uv1 = createTestUserVacancy(123L, "vacancy1");
        UserVacancy uv2 = createTestUserVacancy(123L, "vacancy2");
        UserVacancy uv3 = createTestUserVacancy(456L, "vacancy3");
        
        repository.save(uv1);
        repository.save(uv2);
        repository.save(uv3);
        
        List<UserVacancy> userVacancies = repository.findByUserId(123L);
        
        assertThat(userVacancies).hasSize(2);
        assertThat(userVacancies).extracting(UserVacancy::getVacancyId)
            .containsExactlyInAnyOrder("vacancy1", "vacancy2");
    }

    @Test
    void shouldReturnEmptyListWhenNoVacanciesForUser() {
        List<UserVacancy> userVacancies = repository.findByUserId(999L);
        assertThat(userVacancies).isEmpty();
    }

    @Test
    void shouldFindNewUserVacanciesByUserId() {
        UserVacancy newVacancy1 = createTestUserVacancy(123L, "new1");
        newVacancy1.setNew(true);
        
        UserVacancy newVacancy2 = createTestUserVacancy(123L, "new2");
        newVacancy2.setNew(true);
        
        UserVacancy oldVacancy = createTestUserVacancy(123L, "old");
        oldVacancy.setNew(false);
        
        UserVacancy otherUserVacancy = createTestUserVacancy(456L, "other");
        otherUserVacancy.setNew(true);
        
        repository.save(newVacancy1);
        repository.save(newVacancy2);
        repository.save(oldVacancy);
        repository.save(otherUserVacancy);
        
        List<UserVacancy> newVacancies = repository.findNewByUserId(123L);
        
        assertThat(newVacancies).hasSize(2);
        assertThat(newVacancies).extracting(UserVacancy::getVacancyId)
            .containsExactlyInAnyOrder("new1", "new2");
        assertThat(newVacancies).allMatch(UserVacancy::isNew);
    }

    @Test
    void shouldMarkAsNotified() {
        UserVacancy userVacancy = createTestUserVacancy(123L, "test-vacancy");
        userVacancy.setNew(true);
        userVacancy.setNotifiedAt(null);
        
        repository.save(userVacancy);
        
        repository.markAsNotified(userVacancy.getId());
        
        Optional<UserVacancy> found = repository.findById(userVacancy.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isNew()).isFalse();
        assertThat(found.get().getNotifiedAt()).isNotNull();
        assertThat(found.get().getNotifiedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void shouldNotMarkAsNotifiedWhenIdNotFound() {
        repository.markAsNotified("non-existent-id");
        
        // Should not throw exception
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldDeleteByUserId() {
        UserVacancy uv1 = createTestUserVacancy(123L, "vacancy1");
        UserVacancy uv2 = createTestUserVacancy(123L, "vacancy2");
        UserVacancy uv3 = createTestUserVacancy(456L, "vacancy3");
        
        repository.save(uv1);
        repository.save(uv2);
        repository.save(uv3);
        
        repository.deleteByUserId(123L);
        
        assertThat(repository.findByUserId(123L)).isEmpty();
        assertThat(repository.findByUserId(456L)).hasSize(1);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void shouldDeleteByUserIdWhenNoVacanciesExist() {
        repository.deleteByUserId(999L);
        
        // Should not throw exception
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldHandleComplexUserVacancyData() {
        Vacancy vacancy = new Vacancy();
        vacancy.setId("complex-vacancy");
        vacancy.setTitle("Senior Java Developer");
        vacancy.setCompanyName("TechCorp");
        vacancy.setSalaryFrom(150000);
        vacancy.setSalaryTo(200000);
        vacancy.setCurrency("RUB");
        vacancy.setCreatedDate(LocalDateTime.of(2023, 6, 15, 14, 30));
        
        UserVacancy userVacancy = new UserVacancy(123L, vacancy);
        userVacancy.setFoundAt(LocalDateTime.of(2023, 6, 15, 15, 0));
        
        repository.save(userVacancy);
        
        Optional<UserVacancy> found = repository.findById(userVacancy.getId());
        assertThat(found).isPresent();
        
        UserVacancy savedUV = found.get();
        assertThat(savedUV.getVacancy().getTitle()).isEqualTo("Senior Java Developer");
        assertThat(savedUV.getVacancy().getCompanyName()).isEqualTo("TechCorp");
        assertThat(savedUV.getVacancy().getSalaryFrom()).isEqualTo(150000);
        assertThat(savedUV.getFoundAt()).isEqualTo(LocalDateTime.of(2023, 6, 15, 15, 0));
    }

    @Test
    void shouldHandleUserVacancyWithNullVacancy() {
        UserVacancy userVacancy = new UserVacancy();
        userVacancy.setId("test-id");
        userVacancy.setUserId(123L);
        userVacancy.setVacancyId("vacancy-id");
        userVacancy.setVacancy(null);
        
        repository.save(userVacancy);
        
        Optional<UserVacancy> found = repository.findById("test-id");
        assertThat(found).isPresent();
        assertThat(found.get().getVacancy()).isNull();
        assertThat(found.get().getUserId()).isEqualTo(123L);
        assertThat(found.get().getVacancyId()).isEqualTo("vacancy-id");
    }

    @Test
    void shouldHandleUserVacancyLifecycleScenario() {
        UserVacancy userVacancy = createTestUserVacancy(123L, "lifecycle-test");
        
        // Initial state - new and not notified
        assertThat(userVacancy.isNew()).isTrue();
        assertThat(userVacancy.getNotifiedAt()).isNull();
        repository.save(userVacancy);
        
        // Find new vacancies - should include our vacancy
        List<UserVacancy> newVacancies = repository.findNewByUserId(123L);
        assertThat(newVacancies).hasSize(1);
        assertThat(newVacancies.get(0).getId()).isEqualTo(userVacancy.getId());
        
        // Mark as notified
        repository.markAsNotified(userVacancy.getId());
        
        // Find new vacancies - should no longer include our vacancy
        List<UserVacancy> newVacanciesAfter = repository.findNewByUserId(123L);
        assertThat(newVacanciesAfter).isEmpty();
        
        // Verify the vacancy is marked as notified
        Optional<UserVacancy> notifiedVacancy = repository.findById(userVacancy.getId());
        assertThat(notifiedVacancy).isPresent();
        assertThat(notifiedVacancy.get().isNew()).isFalse();
        assertThat(notifiedVacancy.get().getNotifiedAt()).isNotNull();
    }

    @Test
    void shouldHandleMultipleUsersWithNewVacancies() {
        // User 123 has 2 new, 1 old
        repository.save(createNewUserVacancy(123L, "new1"));
        repository.save(createNewUserVacancy(123L, "new2"));
        repository.save(createOldUserVacancy(123L, "old1"));
        
        // User 456 has 1 new, 2 old
        repository.save(createNewUserVacancy(456L, "new3"));
        repository.save(createOldUserVacancy(456L, "old2"));
        repository.save(createOldUserVacancy(456L, "old3"));
        
        // User 789 has no vacancies
        
        assertThat(repository.findNewByUserId(123L)).hasSize(2);
        assertThat(repository.findNewByUserId(456L)).hasSize(1);
        assertThat(repository.findNewByUserId(789L)).isEmpty();
        
        assertThat(repository.findByUserId(123L)).hasSize(3);
        assertThat(repository.findByUserId(456L)).hasSize(3);
        assertThat(repository.findByUserId(789L)).isEmpty();
    }

    @Test
    void shouldPersistDataBetweenRepositoryInstances() {
        UserVacancy userVacancy = createTestUserVacancy(123L, "persistent");
        repository.save(userVacancy);
        
        JsonUserVacancyRepository newRepository = new JsonUserVacancyRepository(tempDir.toString());
        
        List<UserVacancy> vacancies = newRepository.findByUserId(123L);
        assertThat(vacancies).hasSize(1);
        assertThat(vacancies.get(0).getVacancyId()).isEqualTo("persistent");
    }

    @Test
    void shouldHandleDateTimeEdgeCases() {
        UserVacancy userVacancy = createTestUserVacancy(123L, "date-test");
        
        LocalDateTime specificTime = LocalDateTime.of(2023, 12, 31, 23, 59, 59, 999999999);
        userVacancy.setFoundAt(specificTime);
        
        repository.save(userVacancy);
        
        Optional<UserVacancy> found = repository.findById(userVacancy.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFoundAt()).isEqualTo(specificTime);
        
        // Mark as notified and verify notification time precision
        LocalDateTime beforeNotification = LocalDateTime.now();
        repository.markAsNotified(userVacancy.getId());
        LocalDateTime afterNotification = LocalDateTime.now();
        
        Optional<UserVacancy> notified = repository.findById(userVacancy.getId());
        assertThat(notified).isPresent();
        assertThat(notified.get().getNotifiedAt())
            .isBetween(beforeNotification, afterNotification);
    }

    @Test
    void shouldHandleSpecialCharactersInIds() {
        UserVacancy userVacancy = new UserVacancy();
        userVacancy.setId("special@chars_123#456");
        userVacancy.setUserId(123L);
        userVacancy.setVacancyId("vacancy@special#chars");
        
        repository.save(userVacancy);
        
        Optional<UserVacancy> found = repository.findById("special@chars_123#456");
        assertThat(found).isPresent();
        assertThat(found.get().getVacancyId()).isEqualTo("vacancy@special#chars");
    }

    @Test
    void shouldHandleVeryLongIds() {
        String longId = "A".repeat(1000);
        String longVacancyId = "B".repeat(1000);
        
        UserVacancy userVacancy = new UserVacancy();
        userVacancy.setId(longId);
        userVacancy.setUserId(123L);
        userVacancy.setVacancyId(longVacancyId);
        
        repository.save(userVacancy);
        
        Optional<UserVacancy> found = repository.findById(longId);
        assertThat(found).isPresent();
        assertThat(found.get().getVacancyId()).isEqualTo(longVacancyId);
    }

    @Test
    void shouldHandleLargeUserIds() {
        UserVacancy userVacancy = createTestUserVacancy(Long.MAX_VALUE, "max-user");
        repository.save(userVacancy);
        
        List<UserVacancy> found = repository.findByUserId(Long.MAX_VALUE);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getUserId()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void shouldHandleMixedNewAndOldVacanciesForMultipleUsers() {
        // Create a complex scenario with multiple users and mixed vacancy states
        Long[] userIds = {100L, 200L, 300L};
        
        for (Long userId : userIds) {
            for (int i = 0; i < 3; i++) {
                UserVacancy newVacancy = createNewUserVacancy(userId, "new" + userId + "_" + i);
                UserVacancy oldVacancy = createOldUserVacancy(userId, "old" + userId + "_" + i);
                
                repository.save(newVacancy);
                repository.save(oldVacancy);
            }
        }
        
        // Verify each user has correct distribution
        for (Long userId : userIds) {
            assertThat(repository.findByUserId(userId)).hasSize(6);
            assertThat(repository.findNewByUserId(userId)).hasSize(3);
        }
        
        // Mark some vacancies as notified
        repository.markAsNotified(createNewUserVacancy(100L, "new100_0").getId());
        repository.markAsNotified(createNewUserVacancy(200L, "new200_1").getId());
        
        // Verify new counts decreased
        assertThat(repository.findNewByUserId(100L)).hasSize(2);
        assertThat(repository.findNewByUserId(200L)).hasSize(2);
        assertThat(repository.findNewByUserId(300L)).hasSize(3);
    }

    @Test
    void shouldDeleteAllUserVacanciesRegardlessOfState() {
        Long userId = 123L;
        
        repository.save(createNewUserVacancy(userId, "new1"));
        repository.save(createNewUserVacancy(userId, "new2"));
        repository.save(createOldUserVacancy(userId, "old1"));
        repository.save(createOldUserVacancy(userId, "old2"));
        
        // Also create vacancies for another user to ensure they're not affected
        repository.save(createNewUserVacancy(456L, "other_new"));
        repository.save(createOldUserVacancy(456L, "other_old"));
        
        assertThat(repository.findByUserId(userId)).hasSize(4);
        assertThat(repository.findByUserId(456L)).hasSize(2);
        
        repository.deleteByUserId(userId);
        
        assertThat(repository.findByUserId(userId)).isEmpty();
        assertThat(repository.findNewByUserId(userId)).isEmpty();
        assertThat(repository.findByUserId(456L)).hasSize(2);
        assertThat(repository.findAll()).hasSize(2);
    }

    private UserVacancy createTestUserVacancy(Long userId, String vacancyId) {
        Vacancy vacancy = new Vacancy();
        vacancy.setId(vacancyId);
        vacancy.setTitle("Test Vacancy " + vacancyId);
        
        return new UserVacancy(userId, vacancy);
    }

    private UserVacancy createNewUserVacancy(Long userId, String vacancyId) {
        UserVacancy uv = createTestUserVacancy(userId, vacancyId);
        uv.setNew(true);
        return uv;
    }

    private UserVacancy createOldUserVacancy(Long userId, String vacancyId) {
        UserVacancy uv = createTestUserVacancy(userId, vacancyId);
        uv.setNew(false);
        uv.setNotifiedAt(LocalDateTime.now().minusDays(1));
        return uv;
    }
}