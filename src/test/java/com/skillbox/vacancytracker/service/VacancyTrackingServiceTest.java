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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyTrackingServiceTest {
    
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
    void shouldCreateServiceWithAllDependencies() {
        assertThat(service).isNotNull();
    }
    
    @Test
    void shouldInitializeForActiveUsers() {
        BotUser user1 = new BotUser();
        user1.setUserId(1L);
        user1.setActive(true);
        user1.setNotificationTime("09:00");
        user1.setTimezoneOffset(ZoneOffset.UTC);
        
        SearchCriteria criteria1 = new SearchCriteria();
        criteria1.setKeyword("java");
        user1.setSearchCriteria(criteria1);
        
        BotUser user2 = new BotUser();
        user2.setUserId(2L);
        user2.setActive(true);
        user2.setNotificationTime("10:30");
        user2.setTimezoneOffset(ZoneOffset.ofHours(3));
        
        SearchCriteria criteria2 = new SearchCriteria();
        criteria2.setKeyword("python");
        user2.setSearchCriteria(criteria2);
        
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        
        service.restartAllTasks();
        
        verify(userRepository).findAll();
        verify(taskManager, times(2)).scheduleVacancyCheck(any(Long.class), any(Runnable.class));
        verify(taskManager, times(2)).scheduleNotification(any(Long.class), any(LocalTime.class), any(ZoneOffset.class), any(Runnable.class));
    }
    
    @Test
    void shouldSkipInactiveUsersOnInitialize() {
        BotUser activeUser = new BotUser();
        activeUser.setUserId(1L);
        activeUser.setActive(true);
        SearchCriteria activeUserCriteria = new SearchCriteria();
        activeUserCriteria.setKeyword("java");
        activeUser.setSearchCriteria(activeUserCriteria);
        
        BotUser inactiveUser = new BotUser();
        inactiveUser.setUserId(2L);
        inactiveUser.setActive(false);
        inactiveUser.setSearchCriteria(new SearchCriteria());
        
        when(userRepository.findAll()).thenReturn(List.of(activeUser, inactiveUser));
        
        service.restartAllTasks();
        
        verify(taskManager, times(1)).scheduleVacancyCheck(any(), any());
    }
    
    @Test
    void shouldSkipUsersWithoutSearchCriteria() {
        BotUser userWithCriteria = new BotUser();
        userWithCriteria.setUserId(1L);
        userWithCriteria.setActive(true);
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("developer");
        userWithCriteria.setSearchCriteria(criteria);
        
        BotUser userWithoutCriteria = new BotUser();
        userWithoutCriteria.setUserId(2L);
        userWithoutCriteria.setActive(true);
        userWithoutCriteria.setSearchCriteria(null);
        
        when(userRepository.findAll()).thenReturn(List.of(userWithCriteria, userWithoutCriteria));
        
        service.restartAllTasks();
        
        verify(taskManager, times(1)).scheduleVacancyCheck(any(), any());
    }
    
    @Test
    void shouldSkipUsersWithoutNotificationTime() {
        BotUser userWithTime = new BotUser();
        userWithTime.setUserId(1L);
        userWithTime.setActive(true);
        userWithTime.setNotificationTime("09:00");
        userWithTime.setTimezoneOffset(ZoneOffset.UTC);
        SearchCriteria criteriaWithTime = new SearchCriteria();
        criteriaWithTime.setKeyword("manager");
        userWithTime.setSearchCriteria(criteriaWithTime);
        
        BotUser userWithoutTime = new BotUser();
        userWithoutTime.setUserId(2L);
        userWithoutTime.setActive(true);
        userWithoutTime.setNotificationTime(null);
        SearchCriteria criteriaWithoutTime = new SearchCriteria();
        criteriaWithoutTime.setKeyword("analyst");
        userWithoutTime.setSearchCriteria(criteriaWithoutTime);
        
        when(userRepository.findAll()).thenReturn(List.of(userWithTime, userWithoutTime));
        
        service.restartAllTasks();
        
        verify(taskManager, times(2)).scheduleVacancyCheck(any(), any());
        verify(taskManager, times(1)).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldHandleInvalidNotificationTime() {
        BotUser userWithInvalidTime = new BotUser();
        userWithInvalidTime.setUserId(1L);
        userWithInvalidTime.setActive(true);
        userWithInvalidTime.setNotificationTime("invalid-time");
        userWithInvalidTime.setTimezoneOffset(ZoneOffset.UTC);
        SearchCriteria invalidTimeCriteria = new SearchCriteria();
        invalidTimeCriteria.setKeyword("engineer");
        userWithInvalidTime.setSearchCriteria(invalidTimeCriteria);
        
        when(userRepository.findAll()).thenReturn(List.of(userWithInvalidTime));
        
        service.restartAllTasks();
        
        verify(taskManager, times(1)).scheduleVacancyCheck(any(), any());
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldShutdownTaskManager() {
        service.shutdown();
        
        verify(taskManager).shutdown();
    }
    
    @Test
    void shouldHandleEmptyUserList() {
        when(userRepository.findAll()).thenReturn(List.of());
        
        service.restartAllTasks();
        
        verify(userRepository).findAll();
        verify(taskManager, never()).scheduleVacancyCheck(any(), any());
        verify(taskManager, never()).scheduleNotification(any(), any(), any(), any());
    }
    
    @Test
    void shouldCreateVacancyCheckTask() {
        BotUser user = new BotUser();
        user.setUserId(1L);
        user.setActive(true);
        SearchCriteria taskCriteria = new SearchCriteria();
        taskCriteria.setKeyword("tester");
        user.setSearchCriteria(taskCriteria);
        
        when(userRepository.findAll()).thenReturn(List.of(user));
        
        service.restartAllTasks();
        
        verify(taskManager).scheduleVacancyCheck(eq(1L), any(Runnable.class));
    }
}