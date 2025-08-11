package com.skillbox.vacancytracker.presentation;

import com.skillbox.vacancytracker.presentation.dto.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.List;
import java.util.Optional;

public class CommandDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(CommandDispatcher.class);
    
    private final List<BotCommand> commands;
    private final BotCommand defaultCommand;
    
    public CommandDispatcher(List<BotCommand> commands, BotCommand defaultCommand) {
        this.commands = List.copyOf(commands);
        this.defaultCommand = defaultCommand;
        
        logger.info("Command dispatcher initialized with {} commands", commands.size());
        commands.forEach(cmd -> logger.debug("Registered command: {}", cmd.getCommandName()));
    }
    
    public SendMessage dispatch(UserMessage message) {
        try {
            Optional<BotCommand> command = findCommand(message);
            BotCommand commandToExecute = command.orElse(defaultCommand);
            
            logger.debug("Dispatching message to command: {}", commandToExecute.getCommandName());
            return commandToExecute.handle(message);
            
        } catch (Exception e) {
            logger.error("Error processing command", e);
            return createErrorMessage(message.chatId(), "Произошла ошибка при обработке команды. Попробуйте еще раз.");
        }
    }
    
    private Optional<BotCommand> findCommand(UserMessage message) {
        return commands.stream()
                .filter(command -> command.canHandle(message))
                .findFirst();
    }
    
    private SendMessage createErrorMessage(Long chatId, String text) {
        return new SendMessage(chatId.toString(), text);
    }
}