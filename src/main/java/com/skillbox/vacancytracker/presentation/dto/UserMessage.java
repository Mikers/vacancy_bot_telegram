package com.skillbox.vacancytracker.presentation.dto;

import org.telegram.telegrambots.meta.api.objects.Update;

public record UserMessage(
    Long userId,
    Long chatId,
    String text,
    String firstName,
    String lastName,
    String username,
    boolean isCommand,
    String command,
    String commandArgument
) {
    public static UserMessage fromUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            var user = message.getFrom();
            var text = message.getText();
            
            boolean isCommand = text.startsWith("/");
            String command = null;
            String commandArgument = null;
            
            if (isCommand) {
                String[] parts = text.split("\\s+", 2);
                command = parts[0].substring(1); // Remove leading "/"
                commandArgument = parts.length > 1 ? parts[1] : null;
            }
            
            return new UserMessage(
                user.getId(),
                message.getChatId(),
                text,
                user.getFirstName(),
                user.getLastName(),
                user.getUserName(),
                isCommand,
                command,
                commandArgument
            );
        }
        
        if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();
            var user = callbackQuery.getFrom();
            var data = callbackQuery.getData();
            
            return new UserMessage(
                user.getId(),
                callbackQuery.getMessage().getChatId(),
                data,
                user.getFirstName(),
                user.getLastName(),
                user.getUserName(),
                false,
                null,
                null
            );
        }
        
        throw new IllegalArgumentException("Unsupported update type");
    }
}