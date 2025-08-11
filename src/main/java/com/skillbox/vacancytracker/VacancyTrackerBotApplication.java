package com.skillbox.vacancytracker;

import com.skillbox.vacancytracker.config.BotConfig;
import com.skillbox.vacancytracker.config.ConfigurationManager;
import com.skillbox.vacancytracker.presentation.BotCommand;
import com.skillbox.vacancytracker.presentation.CommandDispatcher;
import com.skillbox.vacancytracker.presentation.VacancyTrackerBot;
import com.skillbox.vacancytracker.presentation.command.DefaultCommand;
import com.skillbox.vacancytracker.presentation.command.StartCommand;
import com.skillbox.vacancytracker.repository.JsonUserRepository;
import com.skillbox.vacancytracker.repository.JsonVacancyRepository;
import com.skillbox.vacancytracker.repository.UserRepository;
import com.skillbox.vacancytracker.repository.VacancyRepository;
import com.skillbox.vacancytracker.service.TrudvsemApiClient;
import com.skillbox.vacancytracker.service.UserService;
import com.skillbox.vacancytracker.service.UserServiceImpl;
import com.skillbox.vacancytracker.service.VacancyApiClient;
import com.skillbox.vacancytracker.util.DirectoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class VacancyTrackerBotApplication {
    private static final Logger logger = LoggerFactory.getLogger(VacancyTrackerBotApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting Vacancy Tracker Bot Application...");
        
        try {
            ConfigurationManager configManager = new ConfigurationManager();
            BotConfig config = configManager.loadConfiguration();
            
            if (config.botToken().startsWith("${") || config.botToken().isBlank()) {
                throw new IllegalStateException("Bot token not configured. Please set BOT_TOKEN environment variable.");
            }
            
            VacancyTrackerBotApplication app = new VacancyTrackerBotApplication();
            app.run(config);
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.exit(1);
        }
    }
    
    private void run(BotConfig config) throws Exception {
        DirectoryManager.initializeDirectories(config.dataDirectory());
        
        UserRepository userRepository = new JsonUserRepository(config.dataDirectory());
        VacancyRepository vacancyRepository = new JsonVacancyRepository(config.dataDirectory());
        
        UserService userService = new UserServiceImpl(userRepository);
        VacancyApiClient vacancyApiClient = new TrudvsemApiClient(config);
        
        TelegramClient telegramClient = new OkHttpTelegramClient(config.botToken());
        
        List<BotCommand> commands = List.of(
            new StartCommand(userService)
        );
        BotCommand defaultCommand = new DefaultCommand();
        
        CommandDispatcher commandDispatcher = new CommandDispatcher(commands, defaultCommand);
        
        VacancyTrackerBot bot = new VacancyTrackerBot(config, commandDispatcher, telegramClient);
        
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(config.botToken(), bot);
            logger.info("Vacancy Tracker Bot started successfully");
            
            Thread.currentThread().join();
        }
    }
}