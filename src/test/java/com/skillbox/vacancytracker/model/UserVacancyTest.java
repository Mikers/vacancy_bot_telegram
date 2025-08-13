package com.skillbox.vacancytracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserVacancyTest {

    private UserVacancy userVacancy;
    private Vacancy testVacancy;

    @BeforeEach
    void setUp() {
        testVacancy = new Vacancy();
        testVacancy.setId("test-vacancy-123");
        testVacancy.setTitle("Java Developer");
        
        userVacancy = new UserVacancy();
    }

    @Test
    void shouldCreateEmptyUserVacancy() {
        assertThat(userVacancy.getId()).isNull();
        assertThat(userVacancy.getUserId()).isNull();
        assertThat(userVacancy.getVacancyId()).isNull();
        assertThat(userVacancy.getVacancy()).isNull();
        assertThat(userVacancy.getFoundAt()).isNotNull(); // Set in constructor
        assertThat(userVacancy.getNotifiedAt()).isNull();
        assertThat(userVacancy.isNew()).isTrue(); // Set in constructor
    }

    @Test
    void shouldCreateUserVacancyWithConstructor() {
        Long userId = 123L;
        UserVacancy uv = new UserVacancy(userId, testVacancy);
        
        assertThat(uv.getUserId()).isEqualTo(userId);
        assertThat(uv.getVacancyId()).isEqualTo("test-vacancy-123");
        assertThat(uv.getVacancy()).isEqualTo(testVacancy);
        assertThat(uv.getId()).isEqualTo("123-test-vacancy-123");
        assertThat(uv.getFoundAt()).isNotNull();
        assertThat(uv.isNew()).isTrue();
    }

    @Test
    void shouldSetAndGetId() {
        userVacancy.setId("user-vacancy-456");
        assertThat(userVacancy.getId()).isEqualTo("user-vacancy-456");
    }

    @Test
    void shouldSetAndGetUserId() {
        userVacancy.setUserId(789L);
        assertThat(userVacancy.getUserId()).isEqualTo(789L);
    }

    @Test
    void shouldSetAndGetVacancyId() {
        userVacancy.setVacancyId("vacancy-999");
        assertThat(userVacancy.getVacancyId()).isEqualTo("vacancy-999");
    }

    @Test
    void shouldSetAndGetVacancy() {
        userVacancy.setVacancy(testVacancy);
        assertThat(userVacancy.getVacancy()).isEqualTo(testVacancy);
        assertThat(userVacancy.getVacancy().getId()).isEqualTo("test-vacancy-123");
        assertThat(userVacancy.getVacancy().getTitle()).isEqualTo("Java Developer");
    }

    @Test
    void shouldSetAndGetFoundAt() {
        LocalDateTime foundTime = LocalDateTime.of(2023, 10, 15, 14, 30, 0);
        userVacancy.setFoundAt(foundTime);
        assertThat(userVacancy.getFoundAt()).isEqualTo(foundTime);
    }

    @Test
    void shouldSetAndGetNotifiedAt() {
        LocalDateTime notifiedTime = LocalDateTime.of(2023, 10, 16, 9, 0, 0);
        userVacancy.setNotifiedAt(notifiedTime);
        assertThat(userVacancy.getNotifiedAt()).isEqualTo(notifiedTime);
    }

    @Test
    void shouldSetAndGetNew() {
        userVacancy.setNew(true);
        assertThat(userVacancy.isNew()).isTrue();
        
        userVacancy.setNew(false);
        assertThat(userVacancy.isNew()).isFalse();
    }

    @Test
    void shouldHandleNullValues() {
        userVacancy.setId(null);
        userVacancy.setUserId(null);
        userVacancy.setVacancyId(null);
        userVacancy.setVacancy(null);
        userVacancy.setFoundAt(null);
        userVacancy.setNotifiedAt(null);
        
        assertThat(userVacancy.getId()).isNull();
        assertThat(userVacancy.getUserId()).isNull();
        assertThat(userVacancy.getVacancyId()).isNull();
        assertThat(userVacancy.getVacancy()).isNull();
        assertThat(userVacancy.getFoundAt()).isNull();
        assertThat(userVacancy.getNotifiedAt()).isNull();
    }

    @Test
    void shouldHandleEmptyStringId() {
        userVacancy.setId("");
        userVacancy.setVacancyId("");
        
        assertThat(userVacancy.getId()).isEqualTo("");
        assertThat(userVacancy.getVacancyId()).isEqualTo("");
    }

    @Test
    void shouldHandleSpecialCharactersInIds() {
        userVacancy.setId("user-vacancy@123#456");
        userVacancy.setVacancyId("vacancy_special-chars!@#");
        
        assertThat(userVacancy.getId()).isEqualTo("user-vacancy@123#456");
        assertThat(userVacancy.getVacancyId()).isEqualTo("vacancy_special-chars!@#");
    }

    @Test
    void shouldHandleVeryLongIds() {
        String longId = "A".repeat(500);
        userVacancy.setId(longId);
        userVacancy.setVacancyId(longId);
        
        assertThat(userVacancy.getId()).isEqualTo(longId);
        assertThat(userVacancy.getVacancyId()).isEqualTo(longId);
    }

    @Test
    void shouldHandleZeroAndNegativeUserIds() {
        userVacancy.setUserId(0L);
        assertThat(userVacancy.getUserId()).isEqualTo(0L);
        
        userVacancy.setUserId(-123L);
        assertThat(userVacancy.getUserId()).isEqualTo(-123L);
    }

    @Test
    void shouldHandleVeryLargeUserIds() {
        userVacancy.setUserId(Long.MAX_VALUE);
        assertThat(userVacancy.getUserId()).isEqualTo(Long.MAX_VALUE);
        
        userVacancy.setUserId(Long.MIN_VALUE);
        assertThat(userVacancy.getUserId()).isEqualTo(Long.MIN_VALUE);
    }

    @Test
    void shouldHandlePastAndFutureDates() {
        LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59, 59);
        
        userVacancy.setFoundAt(pastDate);
        userVacancy.setNotifiedAt(futureDate);
        
        assertThat(userVacancy.getFoundAt()).isEqualTo(pastDate);
        assertThat(userVacancy.getNotifiedAt()).isEqualTo(futureDate);
    }

    @Test
    void shouldHandleDateTimeWithNanoseconds() {
        LocalDateTime dateTimeWithNanos = LocalDateTime.of(2023, 6, 15, 10, 30, 45, 123456789);
        
        userVacancy.setFoundAt(dateTimeWithNanos);
        userVacancy.setNotifiedAt(dateTimeWithNanos);
        
        assertThat(userVacancy.getFoundAt()).isEqualTo(dateTimeWithNanos);
        assertThat(userVacancy.getNotifiedAt()).isEqualTo(dateTimeWithNanos);
    }

    @Test
    void shouldTestEqualsWithSameObject() {
        userVacancy.setId("test-id");
        assertThat(userVacancy).isEqualTo(userVacancy);
    }

    @Test
    void shouldTestEqualsWithNull() {
        assertThat(userVacancy).isNotEqualTo(null);
    }

    @Test
    void shouldTestEqualsWithDifferentClass() {
        assertThat(userVacancy).isNotEqualTo("not a UserVacancy");
    }

    @Test
    void shouldTestEqualsWithSameId() {
        userVacancy.setId("same-id");
        
        UserVacancy other = new UserVacancy();
        other.setId("same-id");
        
        assertThat(userVacancy).isEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithDifferentIds() {
        userVacancy.setId("id-1");
        
        UserVacancy other = new UserVacancy();
        other.setId("id-2");
        
        assertThat(userVacancy).isNotEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithNullIds() {
        userVacancy.setId(null);
        
        UserVacancy other = new UserVacancy();
        other.setId(null);
        
        assertThat(userVacancy).isEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithOneNullId() {
        userVacancy.setId("test-id");
        
        UserVacancy other = new UserVacancy();
        other.setId(null);
        
        assertThat(userVacancy).isNotEqualTo(other);
    }

    @Test
    void shouldTestHashCodeConsistency() {
        userVacancy.setId("test-id");
        
        int hashCode1 = userVacancy.hashCode();
        int hashCode2 = userVacancy.hashCode();
        
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void shouldTestHashCodeForEqualObjects() {
        userVacancy.setId("same-id");
        
        UserVacancy other = new UserVacancy();
        other.setId("same-id");
        
        assertThat(userVacancy.hashCode()).isEqualTo(other.hashCode());
    }

    @Test
    void shouldTestHashCodeWithNullId() {
        userVacancy.setId(null);
        
        int hashCode = userVacancy.hashCode();
        
        // Should not throw exception
        assertThat(hashCode).isEqualTo(0);
    }

    @Test
    void shouldCreateIdFromUserIdAndVacancyIdInConstructor() {
        Vacancy vacancy = new Vacancy();
        vacancy.setId("vacancy-456");
        
        UserVacancy uv = new UserVacancy(789L, vacancy);
        
        assertThat(uv.getId()).isEqualTo("789-vacancy-456");
        assertThat(uv.getUserId()).isEqualTo(789L);
        assertThat(uv.getVacancyId()).isEqualTo("vacancy-456");
    }

    @Test
    void shouldHandleVacancyWithNullIdInConstructor() {
        Vacancy vacancy = new Vacancy();
        vacancy.setId(null);
        
        UserVacancy uv = new UserVacancy(123L, vacancy);
        
        assertThat(uv.getId()).isEqualTo("123-null");
        assertThat(uv.getUserId()).isEqualTo(123L);
        assertThat(uv.getVacancyId()).isNull();
    }

    @Test
    void shouldHandleNullUserIdInConstructor() {
        Vacancy vacancy = new Vacancy();
        vacancy.setId("vacancy-123");
        
        UserVacancy uv = new UserVacancy(null, vacancy);
        
        assertThat(uv.getId()).isEqualTo("null-vacancy-123");
        assertThat(uv.getUserId()).isNull();
        assertThat(uv.getVacancyId()).isEqualTo("vacancy-123");
    }

    @Test
    void shouldSetFoundAtToCurrentTimeInDefaultConstructor() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        UserVacancy uv = new UserVacancy();
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);
        
        assertThat(uv.getFoundAt()).isBetween(beforeCreation, afterCreation);
    }

    @Test
    void shouldSetFoundAtToCurrentTimeInParameterizedConstructor() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        UserVacancy uv = new UserVacancy(123L, testVacancy);
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);
        
        assertThat(uv.getFoundAt()).isBetween(beforeCreation, afterCreation);
    }

    @Test
    void shouldAllowChangingNewFlag() {
        UserVacancy uv = new UserVacancy(123L, testVacancy);
        
        assertThat(uv.isNew()).isTrue(); // Default from constructor
        
        uv.setNew(false);
        assertThat(uv.isNew()).isFalse();
        
        uv.setNew(true);
        assertThat(uv.isNew()).isTrue();
    }

    @Test
    void shouldHandleComplexScenario() {
        // Create a complex vacancy
        Vacancy vacancy = new Vacancy();
        vacancy.setId("complex-vacancy-123");
        vacancy.setTitle("Senior Java Developer");
        vacancy.setCompanyName("TechCorp Ltd.");
        vacancy.setSalaryFrom(150000);
        vacancy.setSalaryTo(200000);
        vacancy.setCurrency("RUB");
        
        // Create UserVacancy with constructor
        Long userId = 12345L;
        UserVacancy uv = new UserVacancy(userId, vacancy);
        
        // Verify initial state
        assertThat(uv.getId()).isEqualTo("12345-complex-vacancy-123");
        assertThat(uv.getUserId()).isEqualTo(userId);
        assertThat(uv.getVacancyId()).isEqualTo("complex-vacancy-123");
        assertThat(uv.getVacancy()).isEqualTo(vacancy);
        assertThat(uv.isNew()).isTrue();
        assertThat(uv.getFoundAt()).isNotNull();
        assertThat(uv.getNotifiedAt()).isNull();
        
        // Simulate processing
        LocalDateTime notificationTime = LocalDateTime.now();
        uv.setNotifiedAt(notificationTime);
        uv.setNew(false);
        
        // Verify processed state
        assertThat(uv.getNotifiedAt()).isEqualTo(notificationTime);
        assertThat(uv.isNew()).isFalse();
        
        // Verify vacancy details are still accessible
        assertThat(uv.getVacancy().getTitle()).isEqualTo("Senior Java Developer");
        assertThat(uv.getVacancy().getCompanyName()).isEqualTo("TechCorp Ltd.");
        assertThat(uv.getVacancy().getSalaryFrom()).isEqualTo(150000);
        assertThat(uv.getVacancy().getSalaryTo()).isEqualTo(200000);
    }

    @Test
    void shouldMaintainStateAfterMultipleChanges() {
        UserVacancy uv = new UserVacancy(999L, testVacancy);
        
        // Initial state
        String originalId = uv.getId();
        LocalDateTime originalFoundAt = uv.getFoundAt();
        
        // Make multiple changes
        uv.setUserId(888L);
        uv.setVacancyId("new-vacancy-id");
        uv.setNew(false);
        
        LocalDateTime newNotifiedTime = LocalDateTime.now();
        uv.setNotifiedAt(newNotifiedTime);
        
        Vacancy newVacancy = new Vacancy();
        newVacancy.setId("another-vacancy");
        newVacancy.setTitle("Python Developer");
        uv.setVacancy(newVacancy);
        
        // Verify changes
        assertThat(uv.getId()).isEqualTo(originalId); // ID doesn't change automatically
        assertThat(uv.getUserId()).isEqualTo(888L);
        assertThat(uv.getVacancyId()).isEqualTo("new-vacancy-id");
        assertThat(uv.getFoundAt()).isEqualTo(originalFoundAt); // Shouldn't change
        assertThat(uv.getNotifiedAt()).isEqualTo(newNotifiedTime);
        assertThat(uv.isNew()).isFalse();
        assertThat(uv.getVacancy()).isEqualTo(newVacancy);
        assertThat(uv.getVacancy().getTitle()).isEqualTo("Python Developer");
    }
}