package com.skillbox.vacancytracker.repository;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JsonUserRepositoryTest {

    @TempDir
    Path tempDir;

    private JsonUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JsonUserRepository(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir.resolve("users.json"))) {
            Files.delete(tempDir.resolve("users.json"));
        }
    }

    @Test
    void shouldCreateRepositoryWithUsersJsonFile() {
        assertThat(tempDir.resolve("users.json")).exists();
    }

    @Test
    void shouldSaveAndRetrieveUser() {
        BotUser user = createTestUser(123L, 456L, "testuser");
        
        repository.save(user);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(123L);
        assertThat(found.get().getChatId()).isEqualTo(456L);
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldUpdateExistingUser() {
        BotUser user = createTestUser(123L, 456L, "original");
        repository.save(user);
        
        user.setUsername("updated");
        user.setFirstName("UpdatedName");
        user.setTimezoneOffset(ZoneOffset.ofHours(3));
        repository.save(user);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("updated");
        assertThat(found.get().getFirstName()).isEqualTo("UpdatedName");
        assertThat(found.get().getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(3));
    }

    @Test
    void shouldDeleteUser() {
        BotUser user = createTestUser(123L, 456L, "testuser");
        repository.save(user);
        
        assertThat(repository.exists(123L)).isTrue();
        
        repository.delete(123L);
        
        assertThat(repository.exists(123L)).isFalse();
        assertThat(repository.findById(123L)).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        BotUser user1 = createTestUser(1L, 11L, "user1");
        BotUser user2 = createTestUser(2L, 22L, "user2");
        BotUser user3 = createTestUser(3L, 33L, "user3");
        
        repository.save(user1);
        repository.save(user2);
        repository.save(user3);
        
        List<BotUser> allUsers = repository.findAll();
        assertThat(allUsers).hasSize(3);
        assertThat(allUsers).extracting(BotUser::getUsername)
            .containsExactlyInAnyOrder("user1", "user2", "user3");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        List<BotUser> allUsers = repository.findAll();
        assertThat(allUsers).isEmpty();
    }

    @Test
    void shouldHandleUserWithComplexSearchCriteria() {
        BotUser user = createTestUser(123L, 456L, "developer");
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Java Senior Developer");
        criteria.setRegionCode(77);
        criteria.setMinimumSalary(150000);
        criteria.setMinimumExperience(5);
        user.setSearchCriteria(criteria);
        
        repository.save(user);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        
        SearchCriteria savedCriteria = found.get().getSearchCriteria();
        assertThat(savedCriteria.getKeyword()).isEqualTo("Java Senior Developer");
        assertThat(savedCriteria.getRegionCode()).isEqualTo(77);
        assertThat(savedCriteria.getMinimumSalary()).isEqualTo(150000);
        assertThat(savedCriteria.getMinimumExperience()).isEqualTo(5);
    }

    @Test
    void shouldHandleUserWithNotificationSettings() {
        BotUser user = createTestUser(123L, 456L, "notified_user");
        user.setNotificationTime("09:30");
        user.setTimezoneOffset(ZoneOffset.ofHours(3));
        user.setActive(true);
        
        repository.save(user);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getNotificationTime()).isEqualTo("09:30");
        assertThat(found.get().getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void shouldHandleUserWithAllFieldsSet() {
        BotUser user = new BotUser(123L, 456L);
        user.setUsername("complete_user");
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setNotificationTime("18:45");
        user.setTimezoneOffset(ZoneOffset.ofHoursMinutes(5, 30));
        user.setActive(false);
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Backend Developer");
        criteria.setRegionCode(78);
        user.setSearchCriteria(criteria);
        
        repository.save(user);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        
        BotUser savedUser = found.get();
        assertThat(savedUser.getUserId()).isEqualTo(123L);
        assertThat(savedUser.getChatId()).isEqualTo(456L);
        assertThat(savedUser.getUsername()).isEqualTo("complete_user");
        assertThat(savedUser.getFirstName()).isEqualTo("Иван");
        assertThat(savedUser.getLastName()).isEqualTo("Петров");
        assertThat(savedUser.getNotificationTime()).isEqualTo("18:45");
        assertThat(savedUser.getTimezoneOffset()).isEqualTo(ZoneOffset.ofHoursMinutes(5, 30));
        assertThat(savedUser.isActive()).isFalse();
        assertThat(savedUser.getSearchCriteria().getKeyword()).isEqualTo("Backend Developer");
        assertThat(savedUser.getSearchCriteria().getRegionCode()).isEqualTo(78);
    }

    @Test
    void shouldHandleNullFields() {
        BotUser user = new BotUser(123L, 456L);
        user.setUsername(null);
        user.setFirstName(null);
        user.setLastName(null);
        user.setNotificationTime(null);
        user.setTimezoneOffset(null);
        user.setSearchCriteria(null);
        
        repository.save(user);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        
        BotUser savedUser = found.get();
        assertThat(savedUser.getUsername()).isNull();
        assertThat(savedUser.getFirstName()).isNull();
        assertThat(savedUser.getLastName()).isNull();
        assertThat(savedUser.getNotificationTime()).isNull();
        assertThat(savedUser.getTimezoneOffset()).isNull();
        assertThat(savedUser.getSearchCriteria()).isNull();
    }

    @Test
    void shouldPersistDataBetweenRepositoryInstances() {
        BotUser user = createTestUser(123L, 456L, "persistent_user");
        repository.save(user);
        
        JsonUserRepository newRepository = new JsonUserRepository(tempDir.toString());
        
        Optional<BotUser> found = newRepository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("persistent_user");
    }

    @Test
    void shouldHandleMultipleUsersWithDifferentTimezones() {
        BotUser userUtc = createTestUser(1L, 11L, "user_utc");
        userUtc.setTimezoneOffset(ZoneOffset.UTC);
        
        BotUser userMoscow = createTestUser(2L, 22L, "user_moscow");
        userMoscow.setTimezoneOffset(ZoneOffset.ofHours(3));
        
        BotUser userNewYork = createTestUser(3L, 33L, "user_newyork");
        userNewYork.setTimezoneOffset(ZoneOffset.ofHours(-5));
        
        BotUser userTokyo = createTestUser(4L, 44L, "user_tokyo");
        userTokyo.setTimezoneOffset(ZoneOffset.ofHours(9));
        
        repository.save(userUtc);
        repository.save(userMoscow);
        repository.save(userNewYork);
        repository.save(userTokyo);
        
        List<BotUser> allUsers = repository.findAll();
        assertThat(allUsers).hasSize(4);
        
        Optional<BotUser> foundUtc = repository.findById(1L);
        Optional<BotUser> foundMoscow = repository.findById(2L);
        Optional<BotUser> foundNewYork = repository.findById(3L);
        Optional<BotUser> foundTokyo = repository.findById(4L);
        
        assertThat(foundUtc.get().getTimezoneOffset()).isEqualTo(ZoneOffset.UTC);
        assertThat(foundMoscow.get().getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(foundNewYork.get().getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(-5));
        assertThat(foundTokyo.get().getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(9));
    }

    @Test
    void shouldHandleSpecialCharactersInUserData() {
        BotUser user = createTestUser(123L, 456L, "user@special_chars");
        user.setFirstName("Иван-Михаил");
        user.setLastName("O'Connor-Smith");
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("C++ / JavaScript / .NET Developer");
        user.setSearchCriteria(criteria);
        
        repository.save(user);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user@special_chars");
        assertThat(found.get().getFirstName()).isEqualTo("Иван-Михаил");
        assertThat(found.get().getLastName()).isEqualTo("O'Connor-Smith");
        assertThat(found.get().getSearchCriteria().getKeyword()).isEqualTo("C++ / JavaScript / .NET Developer");
    }

    @Test
    void shouldHandleEmptyStrings() {
        BotUser user = createTestUser(123L, 456L, "");
        user.setFirstName("");
        user.setLastName("");
        user.setNotificationTime("");
        
        repository.save(user);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("");
        assertThat(found.get().getFirstName()).isEqualTo("");
        assertThat(found.get().getLastName()).isEqualTo("");
        assertThat(found.get().getNotificationTime()).isEqualTo("");
    }

    @Test
    void shouldHandleVeryLargeUserId() {
        BotUser user = createTestUser(Long.MAX_VALUE, Long.MIN_VALUE, "max_user");
        
        repository.save(user);
        
        Optional<BotUser> found = repository.findById(Long.MAX_VALUE);
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(Long.MAX_VALUE);
        assertThat(found.get().getChatId()).isEqualTo(Long.MIN_VALUE);
    }

    @Test
    void shouldHandleUserWithEmptySearchCriteria() {
        BotUser user = createTestUser(123L, 456L, "empty_criteria_user");
        
        SearchCriteria emptyCriteria = new SearchCriteria();
        user.setSearchCriteria(emptyCriteria);
        
        repository.save(user);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getSearchCriteria()).isNotNull();
        assertThat(found.get().getSearchCriteria().isEmpty()).isTrue();
    }

    @Test
    void shouldReplaceUserWhenSavingWithSameId() {
        BotUser originalUser = createTestUser(123L, 456L, "original");
        originalUser.setFirstName("Original");
        repository.save(originalUser);
        
        BotUser replacementUser = createTestUser(123L, 789L, "replacement");
        replacementUser.setFirstName("Replacement");
        repository.save(replacementUser);
        
        List<BotUser> allUsers = repository.findAll();
        assertThat(allUsers).hasSize(1);
        
        Optional<BotUser> found = repository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("replacement");
        assertThat(found.get().getFirstName()).isEqualTo("Replacement");
        assertThat(found.get().getChatId()).isEqualTo(789L);
    }

    @Test
    void shouldMaintainUserDataIntegrity() {
        // Create users with complex data
        for (int i = 0; i < 5; i++) {
            BotUser user = createTestUser((long) i, (long) (i * 100), "user" + i);
            user.setFirstName("FirstName" + i);
            user.setLastName("LastName" + i);
            user.setNotificationTime(String.format("%02d:00", 8 + i));
            user.setTimezoneOffset(ZoneOffset.ofHours(i - 2));
            user.setActive(i % 2 == 0);
            
            SearchCriteria criteria = new SearchCriteria();
            criteria.setKeyword("Developer " + i);
            criteria.setRegionCode(70 + i);
            criteria.setMinimumSalary(100000 + i * 20000);
            criteria.setMinimumExperience(i + 1);
            user.setSearchCriteria(criteria);
            
            repository.save(user);
        }
        
        // Verify all data was saved correctly
        for (int i = 0; i < 5; i++) {
            Optional<BotUser> found = repository.findById((long) i);
            assertThat(found).isPresent();
            
            BotUser user = found.get();
            assertThat(user.getUsername()).isEqualTo("user" + i);
            assertThat(user.getFirstName()).isEqualTo("FirstName" + i);
            assertThat(user.getLastName()).isEqualTo("LastName" + i);
            assertThat(user.getChatId()).isEqualTo((long) (i * 100));
            assertThat(user.getNotificationTime()).isEqualTo(String.format("%02d:00", 8 + i));
            assertThat(user.getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(i - 2));
            assertThat(user.isActive()).isEqualTo(i % 2 == 0);
            
            SearchCriteria criteria = user.getSearchCriteria();
            assertThat(criteria.getKeyword()).isEqualTo("Developer " + i);
            assertThat(criteria.getRegionCode()).isEqualTo(70 + i);
            assertThat(criteria.getMinimumSalary()).isEqualTo(100000 + i * 20000);
            assertThat(criteria.getMinimumExperience()).isEqualTo(i + 1);
        }
    }

    private BotUser createTestUser(Long userId, Long chatId, String username) {
        BotUser user = new BotUser(userId, chatId);
        user.setUsername(username);
        return user;
    }
}