package com.skillbox.vacancytracker.presentation;

import com.skillbox.vacancytracker.presentation.dto.UserMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface BotCommand {
    boolean canHandle(UserMessage message);
    
    SendMessage handle(UserMessage message);
    
    String getCommandName();
    
    String getDescription();
}