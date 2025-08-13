package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.exception.UserNotFoundException;
import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplErrorTest {
    
    @Mock
    private UserRepository userRepository;
    
    private UserServiceImpl userService;
    
    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }
    
    @Test
    void shouldDeleteExistingUser() {
        Long userId = 123L;
        BotUser user = new BotUser();
        user.setUserId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        userService.delete(userId);
        
        verify(userRepository).findById(userId);
        verify(userRepository).delete(userId);
    }
    
    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        Long userId = 999L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userService.delete(userId))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with id: 999");
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any());
    }
    
    @Test
    void shouldThrowExceptionWhenUpdatingSearchCriteriaForNonExistentUser() {
        Long userId = 123L;
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Java");
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userService.updateSearchCriteria(userId, criteria))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with id: 123");
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void shouldThrowExceptionWhenUpdatingTimezoneForNonExistentUser() {
        Long userId = 456L;
        ZoneOffset timezone = ZoneOffset.ofHours(3);
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userService.updateTimezone(userId, timezone))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with id: 456");
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void shouldThrowExceptionWhenUpdatingNotificationTimeForNonExistentUser() {
        Long userId = 789L;
        String notificationTime = "09:00";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userService.updateNotificationTime(userId, notificationTime))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with id: 789");
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void shouldThrowExceptionWhenDeactivatingNonExistentUser() {
        Long userId = 101L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userService.deactivateUser(userId))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with id: 101");
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void shouldSuccessfullyUpdateSearchCriteriaForExistingUser() {
        Long userId = 123L;
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        
        SearchCriteria newCriteria = new SearchCriteria();
        newCriteria.setKeyword("Python");
        newCriteria.setMinimumSalary(60000);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        userService.updateSearchCriteria(userId, newCriteria);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }
    
    @Test
    void shouldSuccessfullyUpdateTimezoneForExistingUser() {
        Long userId = 456L;
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        
        ZoneOffset newTimezone = ZoneOffset.ofHours(-5);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        userService.updateTimezone(userId, newTimezone);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }
    
    @Test
    void shouldSuccessfullyUpdateNotificationTimeForExistingUser() {
        Long userId = 789L;
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        
        String newNotificationTime = "15:30";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        userService.updateNotificationTime(userId, newNotificationTime);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }
    
    @Test
    void shouldSuccessfullyDeactivateExistingUser() {
        Long userId = 101L;
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        existingUser.setActive(true);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        userService.deactivateUser(userId);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }
}