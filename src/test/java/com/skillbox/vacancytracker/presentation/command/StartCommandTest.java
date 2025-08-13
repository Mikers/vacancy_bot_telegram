package com.skillbox.vacancytracker.presentation.command;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.presentation.dto.UserMessage;
import com.skillbox.vacancytracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {
    
    @Mock
    private UserService userService;
    
    private StartCommand command;
    
    @BeforeEach
    void setUp() {
        command = new StartCommand(userService);
    }
    
    @Test
    void shouldReturnCorrectCommandName() {
        assertThat(command.getCommandName()).isEqualTo("start");
    }
    
    @Test
    void shouldReturnCorrectDescription() {
        assertThat(command.getDescription()).isEqualTo("Начать работу с ботом");
    }
    
    @Test
    void shouldHandleStartCommand() {
        UserMessage message = new UserMessage(
            123L, 456L, "/start", "Test", "User", "testuser", true, "start", null
        );
        assertThat(command.canHandle(message)).isTrue();
    }
    
    @Test
    void shouldNotHandleOtherCommands() {
        UserMessage message = new UserMessage(
            123L, 456L, "/help", "Test", "User", "testuser", true, "help", null
        );
        assertThat(command.canHandle(message)).isFalse();
    }
    
    @Test
    void shouldWelcomeExistingUser() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/start", "Test", "User", "testuser", true, "start", null
        );
        
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        existingUser.setChatId(789L); // Different chat ID
        existingUser.setUsername("oldname");
        existingUser.setActive(false);
        existingUser.getSearchCriteria().setKeyword("Java"); // Make it an existing user with criteria
        
        when(userService.findById(userId)).thenReturn(Optional.of(existingUser));
        
        SendMessage response = command.handle(message);
        
        verify(userService).findById(userId);
        verify(userService, never()).save(any());
        
        assertThat(response.getChatId()).isEqualTo(chatId.toString());
        assertThat(response.getText()).contains("Vacancy Tracker Bot приветствует вас!");
    }
    
    @Test
    void shouldCreateNewUser() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/start", "New", "User", "newuser", true, "start", null
        );
        
        when(userService.findById(userId)).thenReturn(Optional.empty());
        
        SendMessage response = command.handle(message);
        
        verify(userService).findById(userId);
        verify(userService).save(any(BotUser.class));
        
        assertThat(response.getChatId()).isEqualTo(chatId.toString());
        assertThat(response.getText()).contains("Вы успешно зарегистрированы в Vacancy Tracker Bot!");
    }
    
    @Test
    void shouldHandleNullUsername() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/start", "Test", null, null, true, "start", null
        );
        
        when(userService.findById(userId)).thenReturn(Optional.empty());
        
        SendMessage response = command.handle(message);
        
        verify(userService).save(any(BotUser.class));
        assertThat(response.getText()).contains("Вы успешно зарегистрированы в Vacancy Tracker Bot!");
    }
}