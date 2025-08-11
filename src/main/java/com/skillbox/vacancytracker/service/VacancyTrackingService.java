package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.repository.UserRepository;
import com.skillbox.vacancytracker.repository.UserVacancyRepository;
import com.skillbox.vacancytracker.task.NotificationTask;
import com.skillbox.vacancytracker.task.VacancyCheckTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

public class VacancyTrackingService {
    private static final Logger logger = LoggerFactory.getLogger(VacancyTrackingService.class);
    
    private final UserRepository userRepository;
    private final UserVacancyRepository userVacancyRepository;
    private final VacancyApiClient vacancyApiClient;
    private final ScheduledTaskManager taskManager;
    private final TelegramClient telegramClient;
    
    public VacancyTrackingService(UserRepository userRepository,
                                  UserVacancyRepository userVacancyRepository,
                                  VacancyApiClient vacancyApiClient,
                                  ScheduledTaskManager taskManager,
                                  TelegramClient telegramClient) {
        this.userRepository = userRepository;
        this.userVacancyRepository = userVacancyRepository;
        this.vacancyApiClient = vacancyApiClient;
        this.taskManager = taskManager;
        this.telegramClient = telegramClient;
    }
    
    public void startTrackingForUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (!user.isActive()) {
                logger.info("User {} is not active, not starting tracking", userId);
                return;
            }
            
            if (user.getSearchCriteria() == null || user.getSearchCriteria().isEmpty()) {
                logger.info("User {} has no search criteria, not starting tracking", userId);
                return;
            }
            
            scheduleVacancyCheck(user);
            
            if (user.getNotificationTime() != null) {
                scheduleNotification(user);
            }
            
            logger.info("Started tracking for user {}", userId);
        });
    }
    
    public void stopTrackingForUser(Long userId) {
        taskManager.cancelUserTasks(userId);
        userVacancyRepository.deleteByUserId(userId);
        logger.info("Stopped tracking for user {}", userId);
    }
    
    public void updateNotificationTime(Long userId, String timeString, ZoneOffset timezone) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setNotificationTime(timeString);
            user.setTimezoneOffset(timezone);
            userRepository.save(user);
            
            if (user.isActive() && user.getSearchCriteria() != null && !user.getSearchCriteria().isEmpty()) {
                scheduleNotification(user);
            }
            
            logger.info("Updated notification time for user {} to {} {}", userId, timeString, timezone);
        });
    }
    
    public void restartAllTasks() {
        logger.info("Restarting all scheduled tasks");
        
        List<BotUser> activeUsers = userRepository.findAll().stream()
                .filter(BotUser::isActive)
                .filter(u -> u.getSearchCriteria() != null && !u.getSearchCriteria().isEmpty())
                .toList();
        
        for (BotUser user : activeUsers) {
            scheduleVacancyCheck(user);
            if (user.getNotificationTime() != null) {
                scheduleNotification(user);
            }
        }
        
        logger.info("Restarted tasks for {} active users", activeUsers.size());
    }
    
    private void scheduleVacancyCheck(BotUser user) {
        VacancyCheckTask task = new VacancyCheckTask(user, vacancyApiClient, userVacancyRepository);
        taskManager.scheduleVacancyCheck(user.getUserId(), task);
    }
    
    private void scheduleNotification(BotUser user) {
        if (user.getNotificationTime() == null) {
            return;
        }
        
        try {
            LocalTime notificationTime = LocalTime.parse(user.getNotificationTime());
            ZoneOffset timezone = user.getTimezoneOffset() != null ? user.getTimezoneOffset() : ZoneOffset.UTC;
            
            NotificationTask task = new NotificationTask(user, userVacancyRepository, telegramClient);
            taskManager.scheduleNotification(user.getUserId(), notificationTime, timezone, task);
            
        } catch (Exception e) {
            logger.error("Failed to schedule notification for user {}", user.getUserId(), e);
        }
    }
    
    public void shutdown() {
        taskManager.shutdown();
    }
}