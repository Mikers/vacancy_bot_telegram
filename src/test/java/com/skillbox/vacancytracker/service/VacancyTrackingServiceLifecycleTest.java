package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.repository.UserRepository;
import com.skillbox.vacancytracker.repository.UserVacancyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyTrackingServiceLifecycleTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserVacancyRepository userVacancyRepository;
    
    @Mock
    private VacancyApiClient vacancyApiClient;
    
    @Mock
    private ScheduledTaskManager taskManager;
    
    @Mock
    private TelegramClient telegramClient;
    
    private VacancyTrackingService service;
    
    @BeforeEach
    void setUp() {
        service = new VacancyTrackingService(
            userRepository,
            userVacancyRepository, 
            vacancyApiClient,
            taskManager,
            telegramClient
        );
    }
    
    @Test
    void shouldStartTrackingForActiveUserWithCriteria() {
        Long userId = 123L;
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setActive(true);
        user.setNotificationTime("09:00");
        user.setTimezoneOffset(ZoneOffset.UTC);
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Java");
        user.setSearchCriteria(criteria);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        service.startTrackingForUser(userId);
        
        verify(userRepository).findById(userId);
        verify(taskManager).scheduleVacancyCheck(eq(userId), any(Runnable.class));
        verify(taskManager).scheduleNotification(eq(userId), eq(LocalTime.of(9, 0)), eq(ZoneOffset.UTC), any(Runnable.class));
    }
    
    @Test
    void shouldStartTrackingForActiveUserWithoutNotificationTime() {
        Long userId = 456L;
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setActive(true);
        user.setNotificationTime(null);
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setMinimumSalary(50000);
        user.setSearchCriteria(criteria);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        service.startTrackingForUser(userId);
        
        verify(userRepository).findById(userId);
        verify(taskManager).scheduleVacancyCheck(eq(userId), any(Runnable.class));
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldNotStartTrackingForInactiveUser() {
        Long userId = 789L;
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setActive(false);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        service.startTrackingForUser(userId);
        
        verify(userRepository).findById(userId);
        verify(taskManager, never()).scheduleVacancyCheck(any(), any());
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldNotStartTrackingForUserWithoutCriteria() {
        Long userId = 101L;
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setActive(true);
        user.setSearchCriteria(null);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        service.startTrackingForUser(userId);
        
        verify(userRepository).findById(userId);
        verify(taskManager, never()).scheduleVacancyCheck(any(), any());
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldNotStartTrackingForUserWithEmptyCriteria() {
        Long userId = 102L;
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setActive(true);
        user.setSearchCriteria(new SearchCriteria()); // Empty criteria
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        service.startTrackingForUser(userId);
        
        verify(userRepository).findById(userId);
        verify(taskManager, never()).scheduleVacancyCheck(any(), any());
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldNotStartTrackingForNonExistentUser() {
        Long userId = 999L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        service.startTrackingForUser(userId);
        
        verify(userRepository).findById(userId);
        verify(taskManager, never()).scheduleVacancyCheck(any(), any());
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldStopTrackingForUser() {
        Long userId = 123L;
        
        service.stopTrackingForUser(userId);
        
        verify(taskManager).cancelUserTasks(userId);
        verify(userVacancyRepository).deleteByUserId(userId);
    }
    
    @Test
    void shouldUpdateNotificationTimeForExistingActiveUser() {
        Long userId = 123L;
        String timeString = "15:30";
        ZoneOffset timezone = ZoneOffset.ofHours(3);
        
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setActive(true);
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Python");
        user.setSearchCriteria(criteria);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        service.updateNotificationTime(userId, timeString, timezone);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(taskManager).scheduleNotification(eq(userId), eq(LocalTime.of(15, 30)), eq(timezone), any(Runnable.class));
    }
    
    @Test
    void shouldUpdateNotificationTimeForInactiveUser() {
        Long userId = 456L;
        String timeString = "08:00";
        ZoneOffset timezone = ZoneOffset.UTC;
        
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setActive(false);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        service.updateNotificationTime(userId, timeString, timezone);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldUpdateNotificationTimeForUserWithoutCriteria() {
        Long userId = 789L;
        String timeString = "12:00";
        ZoneOffset timezone = ZoneOffset.ofHours(-2);
        
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setActive(true);
        user.setSearchCriteria(null);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        service.updateNotificationTime(userId, timeString, timezone);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldUpdateNotificationTimeForUserWithEmptyCriteria() {
        Long userId = 101L;
        String timeString = "18:45";
        ZoneOffset timezone = ZoneOffset.ofHours(5);
        
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setActive(true);
        user.setSearchCriteria(new SearchCriteria()); // Empty criteria
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        service.updateNotificationTime(userId, timeString, timezone);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldNotUpdateNotificationTimeForNonExistentUser() {
        Long userId = 999L;
        String timeString = "10:00";
        ZoneOffset timezone = ZoneOffset.UTC;
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        service.updateNotificationTime(userId, timeString, timezone);
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldHandleInvalidNotificationTimeFormat() {
        Long userId = 123L;
        String invalidTimeString = "invalid-time-format";
        ZoneOffset timezone = ZoneOffset.UTC;
        
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setActive(true);
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Java");
        user.setSearchCriteria(criteria);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        service.updateNotificationTime(userId, invalidTimeString, timezone);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
}