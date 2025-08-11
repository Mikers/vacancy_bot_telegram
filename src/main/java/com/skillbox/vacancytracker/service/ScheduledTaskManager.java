package com.skillbox.vacancytracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.Map;
import java.util.concurrent.*;

public class ScheduledTaskManager {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskManager.class);
    
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks;
    
    public ScheduledTaskManager() {
        this(Executors.newScheduledThreadPool(5));
    }
    
    public ScheduledTaskManager(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
        this.scheduledTasks = new ConcurrentHashMap<>();
    }
    
    public void scheduleVacancyCheck(Long userId, Runnable task) {
        String taskId = "vacancy-check-" + userId;
        cancelTask(taskId);
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
            wrapTask(task, taskId),
            0,
            24,
            TimeUnit.HOURS
        );
        
        scheduledTasks.put(taskId, future);
        logger.info("Scheduled vacancy check for user {}", userId);
    }
    
    public void scheduleNotification(Long userId, LocalTime notificationTime, 
                                    ZoneOffset userTimezone, Runnable task) {
        String taskId = "notification-" + userId;
        cancelTask(taskId);
        
        long initialDelay = calculateInitialDelay(notificationTime, userTimezone);
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
            wrapTask(task, taskId),
            initialDelay,
            TimeUnit.DAYS.toMinutes(1),
            TimeUnit.MINUTES
        );
        
        scheduledTasks.put(taskId, future);
        logger.info("Scheduled notification for user {} at {} {}", 
                   userId, notificationTime, userTimezone);
    }
    
    public void cancelTask(String taskId) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
            logger.info("Cancelled task: {}", taskId);
        }
    }
    
    public void cancelUserTasks(Long userId) {
        cancelTask("vacancy-check-" + userId);
        cancelTask("notification-" + userId);
    }
    
    public void shutdown() {
        logger.info("Shutting down task scheduler");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private long calculateInitialDelay(LocalTime notificationTime, ZoneOffset userTimezone) {
        ZonedDateTime now = ZonedDateTime.now(userTimezone);
        ZonedDateTime nextNotification = now.with(notificationTime);
        
        if (nextNotification.isBefore(now) || nextNotification.isEqual(now)) {
            nextNotification = nextNotification.plusDays(1);
        }
        
        return Duration.between(now, nextNotification).toMinutes();
    }
    
    private Runnable wrapTask(Runnable task, String taskId) {
        return () -> {
            try {
                logger.debug("Executing task: {}", taskId);
                task.run();
            } catch (Exception e) {
                logger.error("Error executing task: {}", taskId, e);
            }
        };
    }
    
    public boolean isTaskScheduled(String taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        return future != null && !future.isDone() && !future.isCancelled();
    }
    
    public int getActiveTasksCount() {
        return (int) scheduledTasks.values().stream()
            .filter(f -> !f.isDone() && !f.isCancelled())
            .count();
    }
}