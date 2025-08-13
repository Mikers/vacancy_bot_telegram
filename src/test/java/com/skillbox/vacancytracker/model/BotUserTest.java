package com.skillbox.vacancytracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class BotUserTest {

    private BotUser botUser;

    @BeforeEach
    void setUp() {
        botUser = new BotUser();
    }

    @Test
    void shouldCreateDefaultBotUser() {
        assertThat(botUser.getUserId()).isNull();
        assertThat(botUser.getChatId()).isNull();
        assertThat(botUser.getUsername()).isNull();
        assertThat(botUser.getFirstName()).isNull();
        assertThat(botUser.getLastName()).isNull();
        assertThat(botUser.getSearchCriteria()).isNotNull(); // Default created in constructor
        assertThat(botUser.getNotificationTime()).isNull();
        assertThat(botUser.getTimezoneOffset()).isEqualTo(ZoneOffset.UTC); // Default value
        assertThat(botUser.isActive()).isTrue(); // Default value
    }

    @Test
    void shouldCreateBotUserWithConstructor() {
        BotUser user = new BotUser(123L, 456L);
        
        assertThat(user.getUserId()).isEqualTo(123L);
        assertThat(user.getChatId()).isEqualTo(456L);
        assertThat(user.getTimezoneOffset()).isEqualTo(ZoneOffset.UTC);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getSearchCriteria()).isNotNull();
    }

    @Test
    void shouldSetAndGetUserId() {
        botUser.setUserId(12345L);
        assertThat(botUser.getUserId()).isEqualTo(12345L);
    }

    @Test
    void shouldSetAndGetChatId() {
        botUser.setChatId(67890L);
        assertThat(botUser.getChatId()).isEqualTo(67890L);
    }

    @Test
    void shouldSetAndGetUsername() {
        botUser.setUsername("testuser");
        assertThat(botUser.getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldSetAndGetFirstName() {
        botUser.setFirstName("John");
        assertThat(botUser.getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldSetAndGetLastName() {
        botUser.setLastName("Doe");
        assertThat(botUser.getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldSetAndGetSearchCriteria() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Java");
        
        botUser.setSearchCriteria(criteria);
        assertThat(botUser.getSearchCriteria()).isEqualTo(criteria);
        assertThat(botUser.getSearchCriteria().getKeyword()).isEqualTo("Java");
    }

    @Test
    void shouldSetAndGetNotificationTime() {
        String time = "14:30";
        
        botUser.setNotificationTime(time);
        assertThat(botUser.getNotificationTime()).isEqualTo(time);
    }

    @Test
    void shouldSetAndGetTimezoneOffset() {
        ZoneOffset timezone = ZoneOffset.ofHours(3);
        
        botUser.setTimezoneOffset(timezone);
        assertThat(botUser.getTimezoneOffset()).isEqualTo(timezone);
    }

    @Test
    void shouldSetAndGetActive() {
        botUser.setActive(true);
        assertThat(botUser.isActive()).isTrue();
        
        botUser.setActive(false);
        assertThat(botUser.isActive()).isFalse();
    }

    @Test
    void shouldHandleNullValues() {
        botUser.setUserId(null);
        botUser.setChatId(null);
        botUser.setUsername(null);
        botUser.setFirstName(null);
        botUser.setLastName(null);
        botUser.setSearchCriteria(null);
        botUser.setNotificationTime(null);
        botUser.setTimezoneOffset(null);
        
        assertThat(botUser.getUserId()).isNull();
        assertThat(botUser.getChatId()).isNull();
        assertThat(botUser.getUsername()).isNull();
        assertThat(botUser.getFirstName()).isNull();
        assertThat(botUser.getLastName()).isNull();
        assertThat(botUser.getSearchCriteria()).isNull();
        assertThat(botUser.getNotificationTime()).isNull();
        assertThat(botUser.getTimezoneOffset()).isNull();
    }

    @Test
    void shouldHandleEmptyStringValues() {
        botUser.setUsername("");
        botUser.setFirstName("");
        botUser.setLastName("");
        botUser.setNotificationTime("");
        
        assertThat(botUser.getUsername()).isEqualTo("");
        assertThat(botUser.getFirstName()).isEqualTo("");
        assertThat(botUser.getLastName()).isEqualTo("");
        assertThat(botUser.getNotificationTime()).isEqualTo("");
    }

    @Test
    void shouldHandleWhitespaceStringValues() {
        botUser.setUsername("   ");
        botUser.setFirstName("   ");
        botUser.setLastName("   ");
        botUser.setNotificationTime("   ");
        
        assertThat(botUser.getUsername()).isEqualTo("   ");
        assertThat(botUser.getFirstName()).isEqualTo("   ");
        assertThat(botUser.getLastName()).isEqualTo("   ");
        assertThat(botUser.getNotificationTime()).isEqualTo("   ");
    }

    @Test
    void shouldHandleSpecialCharactersInStrings() {
        botUser.setUsername("test@user_123");
        botUser.setFirstName("Иван");
        botUser.setLastName("O'Connor");
        botUser.setNotificationTime("09:30");
        
        assertThat(botUser.getUsername()).isEqualTo("test@user_123");
        assertThat(botUser.getFirstName()).isEqualTo("Иван");
        assertThat(botUser.getLastName()).isEqualTo("O'Connor");
        assertThat(botUser.getNotificationTime()).isEqualTo("09:30");
    }

    @Test
    void shouldHandleVeryLongStrings() {
        String longString = "A".repeat(500);
        
        botUser.setUsername(longString);
        botUser.setFirstName(longString);
        botUser.setLastName(longString);
        
        assertThat(botUser.getUsername()).isEqualTo(longString);
        assertThat(botUser.getFirstName()).isEqualTo(longString);
        assertThat(botUser.getLastName()).isEqualTo(longString);
    }

    @Test
    void shouldHandleZeroAndNegativeIds() {
        botUser.setUserId(0L);
        botUser.setChatId(-123L);
        
        assertThat(botUser.getUserId()).isEqualTo(0L);
        assertThat(botUser.getChatId()).isEqualTo(-123L);
    }

    @Test
    void shouldHandleVeryLargeIds() {
        botUser.setUserId(Long.MAX_VALUE);
        botUser.setChatId(Long.MIN_VALUE);
        
        assertThat(botUser.getUserId()).isEqualTo(Long.MAX_VALUE);
        assertThat(botUser.getChatId()).isEqualTo(Long.MIN_VALUE);
    }

    @Test
    void shouldHandleVariousNotificationTimes() {
        String[] times = {"00:00", "09:30", "12:00", "15:45", "23:59", "08:00:00", "20:15:30"};
        
        for (String time : times) {
            botUser.setNotificationTime(time);
            assertThat(botUser.getNotificationTime()).isEqualTo(time);
        }
    }

    @Test
    void shouldHandlePositiveTimezoneOffsets() {
        ZoneOffset[] positiveOffsets = {
            ZoneOffset.ofHours(1),
            ZoneOffset.ofHours(8),
            ZoneOffset.ofHours(12),
            ZoneOffset.ofHoursMinutes(5, 30),
            ZoneOffset.ofHoursMinutes(9, 45)
        };
        
        for (ZoneOffset offset : positiveOffsets) {
            botUser.setTimezoneOffset(offset);
            assertThat(botUser.getTimezoneOffset()).isEqualTo(offset);
        }
    }

    @Test
    void shouldHandleNegativeTimezoneOffsets() {
        ZoneOffset[] negativeOffsets = {
            ZoneOffset.ofHours(-1),
            ZoneOffset.ofHours(-8),
            ZoneOffset.ofHours(-12),
            ZoneOffset.ofHoursMinutes(-5, -30),
            ZoneOffset.ofHoursMinutes(-9, -45)
        };
        
        for (ZoneOffset offset : negativeOffsets) {
            botUser.setTimezoneOffset(offset);
            assertThat(botUser.getTimezoneOffset()).isEqualTo(offset);
        }
    }

    @Test
    void shouldHandleUtcTimezone() {
        botUser.setTimezoneOffset(ZoneOffset.UTC);
        assertThat(botUser.getTimezoneOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void shouldHandleMinAndMaxTimezoneOffsets() {
        botUser.setTimezoneOffset(ZoneOffset.MIN);
        assertThat(botUser.getTimezoneOffset()).isEqualTo(ZoneOffset.MIN);
        
        botUser.setTimezoneOffset(ZoneOffset.MAX);
        assertThat(botUser.getTimezoneOffset()).isEqualTo(ZoneOffset.MAX);
    }

    @Test
    void shouldTestEqualsWithSameObject() {
        botUser.setUserId(123L);
        assertThat(botUser).isEqualTo(botUser);
    }

    @Test
    void shouldTestEqualsWithNull() {
        assertThat(botUser).isNotEqualTo(null);
    }

    @Test
    void shouldTestEqualsWithDifferentClass() {
        assertThat(botUser).isNotEqualTo("not a BotUser");
    }

    @Test
    void shouldTestEqualsWithSameUserId() {
        botUser.setUserId(123L);
        
        BotUser other = new BotUser();
        other.setUserId(123L);
        
        assertThat(botUser).isEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithDifferentUserIds() {
        botUser.setUserId(123L);
        
        BotUser other = new BotUser();
        other.setUserId(456L);
        
        assertThat(botUser).isNotEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithNullUserIds() {
        botUser.setUserId(null);
        
        BotUser other = new BotUser();
        other.setUserId(null);
        
        assertThat(botUser).isEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithOneNullUserId() {
        botUser.setUserId(123L);
        
        BotUser other = new BotUser();
        other.setUserId(null);
        
        assertThat(botUser).isNotEqualTo(other);
    }

    @Test
    void shouldTestHashCodeConsistency() {
        botUser.setUserId(123L);
        
        int hashCode1 = botUser.hashCode();
        int hashCode2 = botUser.hashCode();
        
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void shouldTestHashCodeForEqualObjects() {
        botUser.setUserId(123L);
        
        BotUser other = new BotUser();
        other.setUserId(123L);
        
        assertThat(botUser.hashCode()).isEqualTo(other.hashCode());
    }

    @Test
    void shouldTestHashCodeWithNullUserId() {
        botUser.setUserId(null);
        
        int hashCode = botUser.hashCode();
        
        // Should not throw exception
        assertThat(hashCode).isEqualTo(0);
    }

    @Test
    void shouldTestToStringWithEmptyUser() {
        String toString = botUser.toString();
        
        assertThat(toString).contains("BotUser{");
        assertThat(toString).contains("userId=null");
        assertThat(toString).contains("chatId=null");
        assertThat(toString).contains("username='null'");
        assertThat(toString).contains("firstName='null'");
        assertThat(toString).contains("active=true");
    }

    @Test
    void shouldTestToStringWithAllFields() {
        botUser.setUserId(123L);
        botUser.setChatId(456L);
        botUser.setUsername("testuser");
        botUser.setFirstName("John");
        botUser.setActive(false);
        
        String toString = botUser.toString();
        
        assertThat(toString).contains("BotUser{");
        assertThat(toString).contains("userId=123");
        assertThat(toString).contains("chatId=456");
        assertThat(toString).contains("username='testuser'");
        assertThat(toString).contains("firstName='John'");
        assertThat(toString).contains("active=false");
    }

    @Test
    void shouldMaintainStateAfterMultipleChanges() {
        // Set initial values
        botUser.setUserId(123L);
        botUser.setChatId(456L);
        botUser.setUsername("user1");
        botUser.setFirstName("John");
        botUser.setLastName("Doe");
        botUser.setNotificationTime("09:00");
        botUser.setTimezoneOffset(ZoneOffset.ofHours(3));
        
        SearchCriteria criteria1 = new SearchCriteria();
        criteria1.setKeyword("Java");
        botUser.setSearchCriteria(criteria1);
        
        botUser.setActive(true);
        
        // Verify initial state
        assertThat(botUser.getUserId()).isEqualTo(123L);
        assertThat(botUser.getChatId()).isEqualTo(456L);
        assertThat(botUser.getUsername()).isEqualTo("user1");
        assertThat(botUser.getFirstName()).isEqualTo("John");
        assertThat(botUser.getLastName()).isEqualTo("Doe");
        assertThat(botUser.getSearchCriteria()).isEqualTo(criteria1);
        assertThat(botUser.getNotificationTime()).isEqualTo("09:00");
        assertThat(botUser.getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(botUser.isActive()).isTrue();
        
        // Change values
        botUser.setUserId(789L);
        botUser.setChatId(101112L);
        botUser.setUsername("user2");
        botUser.setFirstName("Jane");
        botUser.setLastName("Smith");
        botUser.setNotificationTime("18:30");
        botUser.setTimezoneOffset(ZoneOffset.ofHours(-5));
        
        SearchCriteria criteria2 = new SearchCriteria();
        criteria2.setKeyword("Python");
        botUser.setSearchCriteria(criteria2);
        
        botUser.setActive(false);
        
        // Verify changed state
        assertThat(botUser.getUserId()).isEqualTo(789L);
        assertThat(botUser.getChatId()).isEqualTo(101112L);
        assertThat(botUser.getUsername()).isEqualTo("user2");
        assertThat(botUser.getFirstName()).isEqualTo("Jane");
        assertThat(botUser.getLastName()).isEqualTo("Smith");
        assertThat(botUser.getSearchCriteria()).isEqualTo(criteria2);
        assertThat(botUser.getSearchCriteria().getKeyword()).isEqualTo("Python");
        assertThat(botUser.getNotificationTime()).isEqualTo("18:30");
        assertThat(botUser.getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(-5));
        assertThat(botUser.isActive()).isFalse();
    }

    @Test
    void shouldHandleSearchCriteriaWithAllFields() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setRegionCode(77);
        criteria.setMinimumExperience(5);
        criteria.setMinimumSalary(150000);
        criteria.setKeyword("Senior Java Developer");
        
        botUser.setSearchCriteria(criteria);
        
        assertThat(botUser.getSearchCriteria()).isNotNull();
        assertThat(botUser.getSearchCriteria().getRegionCode()).isEqualTo(77);
        assertThat(botUser.getSearchCriteria().getMinimumExperience()).isEqualTo(5);
        assertThat(botUser.getSearchCriteria().getMinimumSalary()).isEqualTo(150000);
        assertThat(botUser.getSearchCriteria().getKeyword()).isEqualTo("Senior Java Developer");
        assertThat(botUser.getSearchCriteria().isEmpty()).isFalse();
    }

    @Test
    void shouldHandleEmptySearchCriteria() {
        SearchCriteria emptyCriteria = new SearchCriteria();
        botUser.setSearchCriteria(emptyCriteria);
        
        assertThat(botUser.getSearchCriteria()).isNotNull();
        assertThat(botUser.getSearchCriteria().isEmpty()).isTrue();
    }

    @Test
    void shouldAllowToggleActiveStateMultipleTimes() {
        boolean originalState = botUser.isActive(); // Should be true by default
        
        for (int i = 0; i < 10; i++) {
            botUser.setActive(!botUser.isActive());
            boolean expectedState = originalState ? (i % 2 != 0) : (i % 2 == 0);
            assertThat(botUser.isActive()).isEqualTo(expectedState);
        }
    }

    @Test
    void shouldHandleComplexUserScenario() {
        // User registration with parametrized constructor
        BotUser user = new BotUser(12345L, 67890L);
        user.setUsername("developer_john");
        user.setFirstName("Иван");
        user.setLastName("Петров");
        
        // User sets up search criteria
        SearchCriteria criteria = new SearchCriteria();
        criteria.setRegionCode(78); // St. Petersburg
        criteria.setMinimumExperience(3);
        criteria.setMinimumSalary(120000);
        criteria.setKeyword("Java Backend Developer");
        user.setSearchCriteria(criteria);
        
        // User configures notifications
        user.setNotificationTime("09:30");
        user.setTimezoneOffset(ZoneOffset.ofHours(3)); // MSK
        
        // Verify complete setup
        assertThat(user.getUserId()).isEqualTo(12345L);
        assertThat(user.getChatId()).isEqualTo(67890L);
        assertThat(user.getUsername()).isEqualTo("developer_john");
        assertThat(user.getFirstName()).isEqualTo("Иван");
        assertThat(user.getLastName()).isEqualTo("Петров");
        assertThat(user.isActive()).isTrue();
        
        assertThat(user.getSearchCriteria().getRegionCode()).isEqualTo(78);
        assertThat(user.getSearchCriteria().getMinimumExperience()).isEqualTo(3);
        assertThat(user.getSearchCriteria().getMinimumSalary()).isEqualTo(120000);
        assertThat(user.getSearchCriteria().getKeyword()).isEqualTo("Java Backend Developer");
        assertThat(user.getSearchCriteria().isEmpty()).isFalse();
        
        assertThat(user.getNotificationTime()).isEqualTo("09:30");
        assertThat(user.getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(3));
        
        // User deactivates account
        user.setActive(false);
        assertThat(user.isActive()).isFalse();
        
        // User reactivates and updates criteria
        user.setActive(true);
        user.getSearchCriteria().setKeyword("Senior Java Developer");
        user.setNotificationTime("18:00");
        
        assertThat(user.isActive()).isTrue();
        assertThat(user.getSearchCriteria().getKeyword()).isEqualTo("Senior Java Developer");
        assertThat(user.getNotificationTime()).isEqualTo("18:00");
    }
}