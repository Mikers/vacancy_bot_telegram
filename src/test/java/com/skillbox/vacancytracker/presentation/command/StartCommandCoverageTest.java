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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartCommandCoverageTest {
    
    @Mock
    private UserService userService;
    
    private StartCommand command;
    
    @BeforeEach
    void setUp() {
        command = new StartCommand(userService);
    }
    
    @Test
    void shouldNotHandleNonCommandMessage() {
        UserMessage message = new UserMessage(
            123L, 456L, "just text", "Test", "User", "testuser", false, null, null
        );
        
        assertThat(command.canHandle(message)).isFalse();
    }
    
    @Test
    void shouldTreatExistingUserWithEmptyCriteriaAsNewUser() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/start", "Test", "User", "testuser", true, "start", null
        );
        
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        existingUser.setChatId(chatId);
        // SearchCriteria is empty by default, so user is treated as "new"
        
        when(userService.findById(userId)).thenReturn(Optional.of(existingUser));
        
        SendMessage response = command.handle(message);
        
        // Even existing users with empty criteria get the "new user" message
        assertThat(response.getText()).contains("Вы успешно зарегистрированы в Vacancy Tracker Bot!");
        assertThat(response.getText()).doesNotContain("приветствует вас");
    }
    
    @Test
    void shouldShowRegionCodeInSettings() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/start", "Test", "User", "testuser", true, "start", null
        );
        
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        existingUser.setChatId(chatId);
        existingUser.getSearchCriteria().setRegionCode(77);
        
        when(userService.findById(userId)).thenReturn(Optional.of(existingUser));
        
        SendMessage response = command.handle(message);
        
        assertThat(response.getText()).contains("Регион: [77]");
    }
    
    @Test
    void shouldShowMinimumExperienceInSettings() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/start", "Test", "User", "testuser", true, "start", null
        );
        
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        existingUser.setChatId(chatId);
        existingUser.getSearchCriteria().setMinimumExperience(3);
        
        when(userService.findById(userId)).thenReturn(Optional.of(existingUser));
        
        SendMessage response = command.handle(message);
        
        assertThat(response.getText()).contains("Минимальный опыт: [3 лет]");
    }
    
    @Test
    void shouldShowMinimumSalaryInSettings() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/start", "Test", "User", "testuser", true, "start", null
        );
        
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        existingUser.setChatId(chatId);
        existingUser.getSearchCriteria().setMinimumSalary(100000);
        
        when(userService.findById(userId)).thenReturn(Optional.of(existingUser));
        
        SendMessage response = command.handle(message);
        
        assertThat(response.getText()).contains("Минимальная зарплата: [100000]");
    }
    
    @Test
    void shouldNotShowNullKeywordInSettings() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/start", "Test", "User", "testuser", true, "start", null
        );
        
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        existingUser.setChatId(chatId);
        existingUser.getSearchCriteria().setKeyword(null);
        existingUser.getSearchCriteria().setRegionCode(77); // Make it non-empty but with null keyword
        
        when(userService.findById(userId)).thenReturn(Optional.of(existingUser));
        
        SendMessage response = command.handle(message);
        
        assertThat(response.getText()).doesNotContain("Слово для поиска");
        assertThat(response.getText()).contains("Регион: [77]");
    }
    
    @Test
    void shouldNotShowEmptyKeywordInSettings() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/start", "Test", "User", "testuser", true, "start", null
        );
        
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        existingUser.setChatId(chatId);
        existingUser.getSearchCriteria().setKeyword("");
        existingUser.getSearchCriteria().setRegionCode(77); // Make it non-empty but with empty keyword
        
        when(userService.findById(userId)).thenReturn(Optional.of(existingUser));
        
        SendMessage response = command.handle(message);
        
        assertThat(response.getText()).doesNotContain("Слово для поиска");
        assertThat(response.getText()).contains("Регион: [77]");
    }
    
    @Test
    void shouldShowAllCriteriaInSettings() {
        Long userId = 123L;
        Long chatId = 456L;
        UserMessage message = new UserMessage(
            userId, chatId, "/start", "Test", "User", "testuser", true, "start", null
        );
        
        BotUser existingUser = new BotUser();
        existingUser.setUserId(userId);
        existingUser.setChatId(chatId);
        
        SearchCriteria criteria = existingUser.getSearchCriteria();
        criteria.setRegionCode(77);
        criteria.setMinimumExperience(5);
        criteria.setMinimumSalary(150000);
        criteria.setKeyword("Java Developer");
        
        when(userService.findById(userId)).thenReturn(Optional.of(existingUser));
        
        SendMessage response = command.handle(message);
        
        String text = response.getText();
        assertThat(text).contains("Регион: [77]");
        assertThat(text).contains("Минимальный опыт: [5 лет]");
        assertThat(text).contains("Минимальная зарплата: [150000]");
        assertThat(text).contains("Слово для поиска: [Java Developer]");
    }
}