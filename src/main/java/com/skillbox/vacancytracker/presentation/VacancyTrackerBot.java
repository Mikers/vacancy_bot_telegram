package com.skillbox.vacancytracker.presentation;

import com.skillbox.vacancytracker.config.BotConfig;
import com.skillbox.vacancytracker.presentation.dto.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class VacancyTrackerBot implements LongPollingSingleThreadUpdateConsumer {
    private static final Logger logger = LoggerFactory.getLogger(VacancyTrackerBot.class);
    
    private final BotConfig config;
    private final CommandDispatcher commandDispatcher;
    private final TelegramClient telegramClient;
    
    public VacancyTrackerBot(BotConfig config, CommandDispatcher commandDispatcher, TelegramClient telegramClient) {
        this.config = config;
        this.commandDispatcher = commandDispatcher;
        this.telegramClient = telegramClient;
        logger.info("VacancyTrackerBot initialized");
    }
    
    @Override
    public void consume(Update update) {
        try {
            if (!isValidUpdate(update)) {
                logger.debug("Ignoring invalid update: {}", update);
                return;
            }
            
            UserMessage userMessage = UserMessage.fromUpdate(update);
            logger.debug("Processing message from user {}: {}", userMessage.userId(), userMessage.text());
            
            SendMessage response = commandDispatcher.dispatch(userMessage);
            
            if (response != null) {
                executeMethod(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing update", e);
            handleError(update, e);
        }
    }
    
    private boolean isValidUpdate(Update update) {
        return (update.hasMessage() && update.getMessage().hasText()) || 
               update.hasCallbackQuery();
    }
    
    private void executeMethod(SendMessage method) {
        try {
            telegramClient.execute(method);
            logger.debug("Successfully executed method: {}", method.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Failed to execute method: {}", method.getClass().getSimpleName(), e);
        }
    }
    
    private void handleError(Update update, Exception e) {
        try {
            Long chatId = null;
            if (update.hasMessage()) {
                chatId = update.getMessage().getChatId();
            } else if (update.hasCallbackQuery()) {
                chatId = update.getCallbackQuery().getMessage().getChatId();
            }
            
            if (chatId != null) {
                SendMessage errorMessage = new SendMessage(
                    chatId.toString(),
                    "Извините, произошла ошибка при обработке вашего запроса. Попробуйте еще раз."
                );
                executeMethod(errorMessage);
            }
        } catch (Exception secondaryError) {
            logger.error("Failed to send error message", secondaryError);
        }
    }
    
    public String getBotToken() {
        return config.botToken();
    }
    
    public String getBotName() {
        return config.botName();
    }
}