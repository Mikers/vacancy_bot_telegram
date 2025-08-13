package com.skillbox.vacancytracker.presentation;

import com.skillbox.vacancytracker.config.BotConfig;
import com.skillbox.vacancytracker.presentation.dto.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyTrackerBotTest {
    
    @Mock
    private CommandDispatcher commandDispatcher;
    
    @Mock
    private TelegramClient telegramClient;
    
    private BotConfig config;
    private VacancyTrackerBot bot;
    
    @BeforeEach
    void setUp() {
        config = new BotConfig("test-token", "test-bot", "test-api-key", "test-url");
        bot = new VacancyTrackerBot(config, commandDispatcher, telegramClient);
    }
    
    @Test
    void shouldCreateBotWithValidConfig() {
        assertThat(bot).isNotNull();
    }
    
    @Test
    void shouldProcessValidUpdateWithMessage() {
        Update update = mock(Update.class);
        var message = mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        var user = mock(org.telegram.telegrambots.meta.api.objects.User.class);
        
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/start");
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(456L);
        when(user.getId()).thenReturn(123L);
        when(user.getFirstName()).thenReturn("Test");
        when(user.getLastName()).thenReturn("User");
        when(user.getUserName()).thenReturn("testuser");
        
        SendMessage expectedResponse = SendMessage.builder()
            .chatId("456")
            .text("Response")
            .build();
        
        when(commandDispatcher.dispatch(any(UserMessage.class))).thenReturn(expectedResponse);
        
        bot.consume(update);
        
        verify(commandDispatcher).dispatch(any(UserMessage.class));
    }
    
    @Test
    void shouldProcessCallbackQuery() {
        Update update = mock(Update.class);
        var callbackQuery = mock(org.telegram.telegrambots.meta.api.objects.CallbackQuery.class);
        var user = mock(org.telegram.telegrambots.meta.api.objects.User.class);
        var message = mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        
        when(update.hasMessage()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getFrom()).thenReturn(user);
        when(callbackQuery.getData()).thenReturn("menu_region");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(456L);
        when(user.getId()).thenReturn(123L);
        when(user.getFirstName()).thenReturn("Test");
        when(user.getLastName()).thenReturn("User");
        when(user.getUserName()).thenReturn("testuser");
        
        SendMessage expectedResponse = SendMessage.builder()
            .chatId("456")
            .text("Callback response")
            .build();
        
        when(commandDispatcher.dispatch(any(UserMessage.class))).thenReturn(expectedResponse);
        
        bot.consume(update);
        
        verify(commandDispatcher).dispatch(any(UserMessage.class));
    }
    
    @Test
    void shouldHandleNullResponseFromDispatcher() {
        Update update = mock(Update.class);
        var message = mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        var user = mock(org.telegram.telegrambots.meta.api.objects.User.class);
        
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/unknown");
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(456L);
        when(user.getId()).thenReturn(123L);
        when(user.getFirstName()).thenReturn("Test");
        when(user.getLastName()).thenReturn("User");
        when(user.getUserName()).thenReturn("testuser");
        
        when(commandDispatcher.dispatch(any(UserMessage.class))).thenReturn(null);
        
        bot.consume(update);
        
        verify(commandDispatcher).dispatch(any(UserMessage.class));
    }
    
    @Test
    void shouldIgnoreNonTextMessage() {
        Update update = mock(Update.class);
        var message = mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(false);
        
        bot.consume(update);
        
        verify(commandDispatcher, never()).dispatch(any());
    }
    
    @Test
    void shouldIgnoreUpdateWithoutMessageOrCallback() {
        Update update = mock(Update.class);
        
        when(update.hasMessage()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(false);
        
        bot.consume(update);
        
        verify(commandDispatcher, never()).dispatch(any());
    }
    
    @Test
    void shouldHandleNullUsername() {
        Update update = mock(Update.class);
        var message = mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        var user = mock(org.telegram.telegrambots.meta.api.objects.User.class);
        
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/start");
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(456L);
        when(user.getId()).thenReturn(123L);
        when(user.getFirstName()).thenReturn("Test");
        when(user.getLastName()).thenReturn("User");
        when(user.getUserName()).thenReturn(null);
        
        SendMessage expectedResponse = SendMessage.builder()
            .chatId("456")
            .text("Response")
            .build();
        
        when(commandDispatcher.dispatch(argThat(userMessage -> userMessage.username() == null))).thenReturn(expectedResponse);
        
        bot.consume(update);
        
        verify(commandDispatcher).dispatch(argThat(userMessage -> userMessage.username() == null));
    }
    
    @Test
    void shouldParseCommandWithArguments() {
        Update update = mock(Update.class);
        var message = mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        var user = mock(org.telegram.telegrambots.meta.api.objects.User.class);
        
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/test arg");
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(456L);
        when(user.getId()).thenReturn(123L);
        when(user.getFirstName()).thenReturn("Test");
        when(user.getLastName()).thenReturn("User");
        when(user.getUserName()).thenReturn("testuser");
        
        SendMessage expectedResponse = SendMessage.builder()
            .chatId("456")
            .text("Success")
            .build();
        
        when(commandDispatcher.dispatch(argThat(userMessage -> 
            userMessage.isCommand() && 
            userMessage.command().equals("test") &&
            "arg".equals(userMessage.commandArgument())
        ))).thenReturn(expectedResponse);
        
        bot.consume(update);
        
        verify(commandDispatcher).dispatch(argThat(userMessage -> 
            userMessage.isCommand() && 
            userMessage.command().equals("test") &&
            "arg".equals(userMessage.commandArgument())
        ));
    }
    
    @Test
    void shouldHandleExceptionDuringProcessing() {
        Update update = mock(Update.class);
        var message = mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        var user = mock(org.telegram.telegrambots.meta.api.objects.User.class);
        
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/error");
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(456L);
        when(user.getId()).thenReturn(123L);
        when(user.getFirstName()).thenReturn("Test");
        when(user.getLastName()).thenReturn("User");
        when(user.getUserName()).thenReturn("testuser");
        
        when(commandDispatcher.dispatch(any(UserMessage.class))).thenThrow(new RuntimeException("Test error"));
        
        bot.consume(update);
        
        verify(commandDispatcher).dispatch(any(UserMessage.class));
    }
    
    @Test
    void shouldCreateBotWithAllParameters() {
        BotConfig newConfig = new BotConfig("token2", "bot2", "key2", "url2");
        VacancyTrackerBot newBot = new VacancyTrackerBot(newConfig, commandDispatcher, telegramClient);
        
        assertThat(newBot).isNotNull();
        assertThat(newBot).isNotSameAs(bot);
    }
}