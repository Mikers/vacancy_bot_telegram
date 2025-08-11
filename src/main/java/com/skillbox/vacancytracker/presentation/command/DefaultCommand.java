package com.skillbox.vacancytracker.presentation.command;

import com.skillbox.vacancytracker.presentation.BotCommand;
import com.skillbox.vacancytracker.presentation.dto.UserMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class DefaultCommand implements BotCommand {
    
    @Override
    public boolean canHandle(UserMessage message) {
        return true; // Default command handles everything
    }
    
    @Override
    public SendMessage handle(UserMessage message) {
        return new SendMessage(
            message.chatId().toString(),
            "Команда не распознана. Используйте /menu для просмотра доступных команд."
        );
    }
    
    @Override
    public String getCommandName() {
        return "default";
    }
    
    @Override
    public String getDescription() {
        return "Обработчик неизвестных команд";
    }
}