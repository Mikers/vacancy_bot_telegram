package com.skillbox.vacancytracker.presentation.command;

import com.skillbox.vacancytracker.presentation.dto.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultCommandTest {

    private DefaultCommand defaultCommand;

    @BeforeEach
    void setUp() {
        defaultCommand = new DefaultCommand();
    }

    @Test
    void shouldReturnCorrectCommandName() {
        assertThat(defaultCommand.getCommandName()).isEqualTo("default");
    }

    @Test
    void shouldReturnCorrectDescription() {
        assertThat(defaultCommand.getDescription()).isEqualTo("Обработчик неизвестных команд");
    }

    @Test
    void shouldHandleAnyMessage() {
        UserMessage message1 = new UserMessage(1L, 100L, "/unknown", "John", "Doe", "user1", true, "unknown", null);
        UserMessage message2 = new UserMessage(2L, 200L, "random text", "Jane", "Smith", "user2", false, null, null);
        UserMessage message3 = new UserMessage(3L, 300L, "", "Bob", "Johnson", "user3", false, null, null);
        
        assertThat(defaultCommand.canHandle(message1)).isTrue();
        assertThat(defaultCommand.canHandle(message2)).isTrue();
        assertThat(defaultCommand.canHandle(message3)).isTrue();
    }

    @Test
    void shouldReturnDefaultMessage() {
        UserMessage message = new UserMessage(1L, 100L, "/unknown", "John", "Doe", "user1", true, "unknown", null);
        
        SendMessage result = defaultCommand.handle(message);
        
        assertThat(result.getChatId()).isEqualTo("100");
        assertThat(result.getText()).isEqualTo("Команда не распознана. Используйте /menu для просмотра доступных команд.");
    }

    @Test
    void shouldHandleVariousChatIds() {
        UserMessage message1 = new UserMessage(1L, 123L, "/test", "John", "Doe", "user1", true, "test", null);
        UserMessage message2 = new UserMessage(2L, 456L, "hello", "Jane", "Smith", "user2", false, null, null);
        
        SendMessage result1 = defaultCommand.handle(message1);
        SendMessage result2 = defaultCommand.handle(message2);
        
        assertThat(result1.getChatId()).isEqualTo("123");
        assertThat(result2.getChatId()).isEqualTo("456");
        assertThat(result1.getText()).isEqualTo(result2.getText());
    }

    @Test
    void shouldHandleEmptyText() {
        UserMessage message = new UserMessage(1L, 100L, "", "John", "Doe", "user1", false, null, null);
        
        SendMessage result = defaultCommand.handle(message);
        
        assertThat(result.getChatId()).isEqualTo("100");
        assertThat(result.getText()).isEqualTo("Команда не распознана. Используйте /menu для просмотра доступных команд.");
    }

    @Test
    void shouldHandleNullText() {
        UserMessage message = new UserMessage(1L, 100L, null, "John", "Doe", "user1", false, null, null);
        
        SendMessage result = defaultCommand.handle(message);
        
        assertThat(result.getChatId()).isEqualTo("100");
        assertThat(result.getText()).isEqualTo("Команда не распознана. Используйте /menu для просмотра доступных команд.");
    }
}