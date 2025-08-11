package com.skillbox.vacancytracker;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramApiTest {
    
    @Test
    void shouldCreateSendMessage() {
        SendMessage message = new SendMessage("123", "test");
        
        assertThat(message.getChatId()).isEqualTo("123");
        assertThat(message.getText()).isEqualTo("test");
    }
}