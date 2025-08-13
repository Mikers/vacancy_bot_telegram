package com.skillbox.vacancytracker.presentation.command;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuCommandTest {
    
    @Mock
    private UserService userService;
    
    private MenuCommand command;
    
    @BeforeEach
    void setUp() {
        command = new MenuCommand(userService);
    }
    
    @Test
    void shouldReturnCorrectCommandName() {
        assertThat(command.getCommandName()).isEqualTo("/menu");
    }
    
    @Test
    void shouldReturnCorrectDescription() {
        assertThat(command.getDescription()).isEqualTo("Открыть главное меню");
    }
    
    @Test
    void shouldHandleMenuCommand() {
        UserMessage message = new UserMessage(
            123L, 456L, "/menu", "Test", "User", "testuser", true, "menu", null
        );
        assertThat(command.canHandle(message)).isTrue();
    }
    
    @Test
    void shouldHandleMenuText() {
        UserMessage message = new UserMessage(
            123L, 456L, "menu", "Test", "User", "testuser", false, null, null
        );
        assertThat(command.canHandle(message)).isTrue();
    }
    
    @Test
    void shouldNotHandleOtherCommands() {
        UserMessage message = new UserMessage(
            123L, 456L, "/start", "Test", "User", "testuser", true, "start", null
        );
        assertThat(command.canHandle(message)).isFalse();
    }
    
    @Test
    void shouldShowMenuForExistingUser() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/menu", "Test", "User", "testuser", true, "menu", null
        );
        
        BotUser user = new BotUser();
        user.setUserId(userId);
        user.setNotificationTime("10:30");
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("java");
        criteria.setMinimumSalary(100000);
        criteria.setMinimumExperience(3);
        criteria.setRegionCode(77);
        user.setSearchCriteria(criteria);
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        
        SendMessage response = command.handle(message);
        
        verify(userService).findById(userId);
        
        assertThat(response.getChatId()).isEqualTo(chatId.toString());
        assertThat(response.getText()).contains("Задайте критерии поиска");
        assertThat(response.getReplyMarkup()).isNotNull();
    }
    
    @Test
    void shouldShowMenuForUserWithNoCriteria() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/menu", "Test", "User", "testuser", true, "menu", null
        );
        
        BotUser user = new BotUser();
        user.setUserId(userId);
        // No search criteria set
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        
        SendMessage response = command.handle(message);
        
        verify(userService).findById(userId);
        
        assertThat(response.getChatId()).isEqualTo(chatId.toString());
        assertThat(response.getText()).contains("Задайте критерии поиска");
        assertThat(response.getReplyMarkup()).isNotNull();
    }
    
    @Test
    void shouldReturnErrorForNonExistentUser() {
        Long userId = 999L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/menu", "Test", "User", "testuser", true, "menu", null
        );
        
        when(userService.findById(userId)).thenReturn(Optional.empty());
        
        SendMessage response = command.handle(message);
        
        verify(userService).findById(userId);
        
        assertThat(response.getChatId()).isEqualTo(chatId.toString());
        assertThat(response.getText()).contains("сначала используйте /start");
    }
}