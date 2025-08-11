package com.skillbox.vacancytracker.presentation.command;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.presentation.BotCommand;
import com.skillbox.vacancytracker.presentation.dto.UserMessage;
import com.skillbox.vacancytracker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class StartCommand implements BotCommand {
    private static final Logger logger = LoggerFactory.getLogger(StartCommand.class);
    
    private final UserService userService;
    
    public StartCommand(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public boolean canHandle(UserMessage message) {
        return message.isCommand() && "start".equals(message.command());
    }
    
    @Override
    public SendMessage handle(UserMessage message) {
        logger.info("Processing /start command for user {}", message.userId());
        
        BotUser user = userService.findById(message.userId())
                .orElseGet(() -> createNewUser(message));
        
        String welcomeText = buildWelcomeMessage(user);
        
        return new SendMessage(message.chatId().toString(), welcomeText);
    }
    
    private BotUser createNewUser(UserMessage message) {
        BotUser user = new BotUser(message.userId(), message.chatId());
        user.setFirstName(message.firstName());
        user.setLastName(message.lastName());
        user.setUsername(message.username());
        
        userService.save(user);
        logger.info("Created new user: {}", user.getUserId());
        
        return user;
    }
    
    private String buildWelcomeMessage(BotUser user) {
        boolean isNewUser = user.getSearchCriteria().isEmpty();
        
        if (isNewUser) {
            return "Вы успешно зарегистрированы в Vacancy Tracker Bot!\n" +
                   "Будет использован часовой пояс UTC, для изменения введите " +
                   "часовой пояс в формате UTC+3.\n\n" +
                   "Используйте команду /menu для получения списка команд.";
        } else {
            return "Vacancy Tracker Bot приветствует вас!\n" +
                   "Вы ранее регистрировались в нашем боте, можете приступать к " +
                   "его использованию.\n" +
                   buildCurrentSettings(user) +
                   "\n\nВведите команду /menu для получения списка команд.";
        }
    }
    
    private String buildCurrentSettings(BotUser user) {
        StringBuilder sb = new StringBuilder("\n\nРанее заданные фильтры:\n");
        
        var criteria = user.getSearchCriteria();
        if (criteria.isEmpty()) {
            sb.append("Фильтры не заданы");
        } else {
            if (criteria.getRegionCode() != null) {
                sb.append("Регион: [").append(criteria.getRegionCode()).append("]\n");
            }
            if (criteria.getMinimumExperience() != null) {
                sb.append("Минимальный опыт: [").append(criteria.getMinimumExperience()).append(" лет]\n");
            }
            if (criteria.getMinimumSalary() != null) {
                sb.append("Минимальная зарплата: [").append(criteria.getMinimumSalary()).append("]\n");
            }
            if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
                sb.append("Слово для поиска: [").append(criteria.getKeyword()).append("]\n");
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String getCommandName() {
        return "start";
    }
    
    @Override
    public String getDescription() {
        return "Начать работу с ботом";
    }
}