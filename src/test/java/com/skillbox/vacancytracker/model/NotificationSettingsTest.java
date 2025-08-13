package com.skillbox.vacancytracker.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationSettingsTest {

    @Test
    void shouldCreateDefaultNotificationSettings() {
        NotificationSettings settings = new NotificationSettings();
        
        assertThat(settings.getNotificationTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(settings.getUserTimezone()).isEqualTo(ZoneOffset.UTC);
        assertThat(settings.isEnabled()).isFalse();
    }

    @Test
    void shouldCreateNotificationSettingsWithParameters() {
        LocalTime time = LocalTime.of(14, 30);
        ZoneOffset timezone = ZoneOffset.ofHours(3);
        
        NotificationSettings settings = new NotificationSettings(time, timezone);
        
        assertThat(settings.getNotificationTime()).isEqualTo(time);
        assertThat(settings.getUserTimezone()).isEqualTo(timezone);
        assertThat(settings.isEnabled()).isTrue();
    }

    @Test
    void shouldSetAndGetNotificationTime() {
        NotificationSettings settings = new NotificationSettings();
        LocalTime newTime = LocalTime.of(18, 45);
        
        settings.setNotificationTime(newTime);
        
        assertThat(settings.getNotificationTime()).isEqualTo(newTime);
    }

    @Test
    void shouldSetAndGetUserTimezone() {
        NotificationSettings settings = new NotificationSettings();
        ZoneOffset newTimezone = ZoneOffset.ofHours(-5);
        
        settings.setUserTimezone(newTimezone);
        
        assertThat(settings.getUserTimezone()).isEqualTo(newTimezone);
    }

    @Test
    void shouldSetAndGetEnabled() {
        NotificationSettings settings = new NotificationSettings();
        
        assertThat(settings.isEnabled()).isFalse();
        
        settings.setEnabled(true);
        assertThat(settings.isEnabled()).isTrue();
        
        settings.setEnabled(false);
        assertThat(settings.isEnabled()).isFalse();
    }

    @Test
    void shouldHandleNullNotificationTime() {
        NotificationSettings settings = new NotificationSettings();
        
        settings.setNotificationTime(null);
        
        assertThat(settings.getNotificationTime()).isNull();
    }

    @Test
    void shouldHandleNullUserTimezone() {
        NotificationSettings settings = new NotificationSettings();
        
        settings.setUserTimezone(null);
        
        assertThat(settings.getUserTimezone()).isNull();
    }

    @Test
    void shouldHandleParameterizedConstructorWithNullValues() {
        NotificationSettings settings = new NotificationSettings(null, null);
        
        assertThat(settings.getNotificationTime()).isNull();
        assertThat(settings.getUserTimezone()).isNull();
        assertThat(settings.isEnabled()).isTrue();
    }

    @Test
    void shouldHandleMidnightNotificationTime() {
        LocalTime midnight = LocalTime.MIDNIGHT;
        NotificationSettings settings = new NotificationSettings();
        
        settings.setNotificationTime(midnight);
        
        assertThat(settings.getNotificationTime()).isEqualTo(midnight);
    }

    @Test
    void shouldHandleEndOfDayNotificationTime() {
        LocalTime endOfDay = LocalTime.of(23, 59, 59);
        NotificationSettings settings = new NotificationSettings();
        
        settings.setNotificationTime(endOfDay);
        
        assertThat(settings.getNotificationTime()).isEqualTo(endOfDay);
    }

    @Test
    void shouldHandleNoonNotificationTime() {
        LocalTime noon = LocalTime.NOON;
        NotificationSettings settings = new NotificationSettings();
        
        settings.setNotificationTime(noon);
        
        assertThat(settings.getNotificationTime()).isEqualTo(noon);
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
        
        NotificationSettings settings = new NotificationSettings();
        
        for (ZoneOffset offset : positiveOffsets) {
            settings.setUserTimezone(offset);
            assertThat(settings.getUserTimezone()).isEqualTo(offset);
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
        
        NotificationSettings settings = new NotificationSettings();
        
        for (ZoneOffset offset : negativeOffsets) {
            settings.setUserTimezone(offset);
            assertThat(settings.getUserTimezone()).isEqualTo(offset);
        }
    }

    @Test
    void shouldHandleUtcTimezone() {
        NotificationSettings settings = new NotificationSettings();
        
        settings.setUserTimezone(ZoneOffset.UTC);
        
        assertThat(settings.getUserTimezone()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void shouldHandleMinAndMaxTimezoneOffsets() {
        NotificationSettings settings = new NotificationSettings();
        
        settings.setUserTimezone(ZoneOffset.MIN);
        assertThat(settings.getUserTimezone()).isEqualTo(ZoneOffset.MIN);
        
        settings.setUserTimezone(ZoneOffset.MAX);
        assertThat(settings.getUserTimezone()).isEqualTo(ZoneOffset.MAX);
    }

    @Test
    void shouldHandleTimeWithNanoseconds() {
        LocalTime timeWithNanos = LocalTime.of(15, 30, 45, 123456789);
        NotificationSettings settings = new NotificationSettings();
        
        settings.setNotificationTime(timeWithNanos);
        
        assertThat(settings.getNotificationTime()).isEqualTo(timeWithNanos);
    }

    @Test
    void shouldMaintainStateAfterMultipleChanges() {
        NotificationSettings settings = new NotificationSettings();
        
        // Initial state
        assertThat(settings.getNotificationTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(settings.getUserTimezone()).isEqualTo(ZoneOffset.UTC);
        assertThat(settings.isEnabled()).isFalse();
        
        // Change everything
        LocalTime newTime = LocalTime.of(20, 15);
        ZoneOffset newTimezone = ZoneOffset.ofHours(7);
        settings.setNotificationTime(newTime);
        settings.setUserTimezone(newTimezone);
        settings.setEnabled(true);
        
        assertThat(settings.getNotificationTime()).isEqualTo(newTime);
        assertThat(settings.getUserTimezone()).isEqualTo(newTimezone);
        assertThat(settings.isEnabled()).isTrue();
        
        // Change again
        LocalTime anotherTime = LocalTime.of(6, 0);
        ZoneOffset anotherTimezone = ZoneOffset.ofHours(-3);
        settings.setNotificationTime(anotherTime);
        settings.setUserTimezone(anotherTimezone);
        settings.setEnabled(false);
        
        assertThat(settings.getNotificationTime()).isEqualTo(anotherTime);
        assertThat(settings.getUserTimezone()).isEqualTo(anotherTimezone);
        assertThat(settings.isEnabled()).isFalse();
    }

    @Test
    void shouldHandleParameterizedConstructorWithEnabledTrue() {
        LocalTime time = LocalTime.of(10, 30);
        ZoneOffset timezone = ZoneOffset.ofHours(2);
        
        NotificationSettings settings = new NotificationSettings(time, timezone);
        
        assertThat(settings.isEnabled()).isTrue();
        
        // Verify we can still change it
        settings.setEnabled(false);
        assertThat(settings.isEnabled()).isFalse();
    }

    @Test
    void shouldAllowToggleEnabledStateMultipleTimes() {
        NotificationSettings settings = new NotificationSettings();
        
        boolean originalState = settings.isEnabled();
        
        for (int i = 0; i < 10; i++) {
            settings.setEnabled(!settings.isEnabled());
            assertThat(settings.isEnabled()).isEqualTo(originalState ? (i % 2 != 0) : (i % 2 == 0));
        }
    }

    @Test
    void shouldHandleCommonTimezoneOffsets() {
        // Test common world timezone offsets
        ZoneOffset[] commonOffsets = {
            ZoneOffset.ofHours(-12), // Baker Island
            ZoneOffset.ofHours(-8),  // PST
            ZoneOffset.ofHours(-5),  // EST
            ZoneOffset.UTC,          // GMT
            ZoneOffset.ofHours(1),   // CET
            ZoneOffset.ofHours(3),   // MSK
            ZoneOffset.ofHours(8),   // CST
            ZoneOffset.ofHours(9),   // JST
            ZoneOffset.ofHours(12)   // NZST
        };
        
        NotificationSettings settings = new NotificationSettings();
        
        for (ZoneOffset offset : commonOffsets) {
            settings.setUserTimezone(offset);
            assertThat(settings.getUserTimezone()).isEqualTo(offset);
        }
    }

    @Test
    void shouldHandleCommonNotificationTimes() {
        LocalTime[] commonTimes = {
            LocalTime.of(6, 0),   // Early morning
            LocalTime.of(9, 0),   // Morning
            LocalTime.of(12, 0),  // Noon
            LocalTime.of(15, 0),  // Afternoon
            LocalTime.of(18, 0),  // Evening
            LocalTime.of(21, 0),  // Night
        };
        
        NotificationSettings settings = new NotificationSettings();
        
        for (LocalTime time : commonTimes) {
            settings.setNotificationTime(time);
            assertThat(settings.getNotificationTime()).isEqualTo(time);
        }
    }
}