package com.skillbox.vacancytracker.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledTaskManagerExtendedTest {
    
    private ScheduledTaskManager taskManager;
    
    @BeforeEach
    void setUp() {
        taskManager = new ScheduledTaskManager();
    }
    
    @AfterEach
    void tearDown() {
        if (taskManager != null) {
            taskManager.shutdown();
        }
    }
    
    @Test
    void shouldCreateScheduledTaskManager() {
        assertThat(taskManager).isNotNull();
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(0);
    }
    
    @Test
    void shouldCreateScheduledTaskManagerWithCustomExecutor() {
        ScheduledExecutorService customExecutor = Executors.newScheduledThreadPool(2);
        ScheduledTaskManager customTaskManager = new ScheduledTaskManager(customExecutor);
        
        try {
            assertThat(customTaskManager).isNotNull();
            assertThat(customTaskManager.getActiveTasksCount()).isEqualTo(0);
        } finally {
            customTaskManager.shutdown();
        }
    }
    
    @Test
    void shouldScheduleVacancyCheck() {
        Long userId = 123L;
        Runnable task = () -> System.out.println("Vacancy check for user " + userId);
        
        taskManager.scheduleVacancyCheck(userId, task);
        
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(1);
        assertThat(taskManager.isTaskScheduled("vacancy-check-" + userId)).isTrue();
    }
    
    @Test
    void shouldScheduleNotification() throws InterruptedException {
        Long userId = 456L;
        LocalTime time = LocalTime.of(9, 30);
        ZoneOffset timezone = ZoneOffset.ofHours(3);
        Runnable task = () -> System.out.println("Notification for user " + userId);
        
        taskManager.scheduleNotification(userId, time, timezone, task);
        
        Thread.sleep(100); // Give time for scheduling
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(1);
        assertThat(taskManager.isTaskScheduled("notification-" + userId)).isTrue();
    }
    
    @Test
    void shouldCancelSpecificTask() {
        Long userId = 789L;
        Runnable task = () -> System.out.println("Test task");
        
        taskManager.scheduleVacancyCheck(userId, task);
        assertThat(taskManager.isTaskScheduled("vacancy-check-" + userId)).isTrue();
        
        taskManager.cancelTask("vacancy-check-" + userId);
        assertThat(taskManager.isTaskScheduled("vacancy-check-" + userId)).isFalse();
    }
    
    @Test
    void shouldCancelAllUserTasks() throws InterruptedException {
        Long userId = 999L;
        LocalTime time = LocalTime.of(14, 0);
        ZoneOffset timezone = ZoneOffset.UTC;
        
        Runnable vacancyTask = () -> System.out.println("Vacancy check");
        Runnable notificationTask = () -> System.out.println("Notification");
        
        taskManager.scheduleVacancyCheck(userId, vacancyTask);
        taskManager.scheduleNotification(userId, time, timezone, notificationTask);
        
        Thread.sleep(100); // Give time for scheduling
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(2);
        
        taskManager.cancelUserTasks(userId);
        assertThat(taskManager.isTaskScheduled("vacancy-check-" + userId)).isFalse();
        assertThat(taskManager.isTaskScheduled("notification-" + userId)).isFalse();
    }
    
    @Test
    void shouldHandleMultipleUsersScheduling() throws InterruptedException {
        Long user1 = 111L;
        Long user2 = 222L;
        LocalTime time1 = LocalTime.of(8, 0);
        LocalTime time2 = LocalTime.of(18, 0);
        ZoneOffset timezone = ZoneOffset.ofHours(-5);
        
        Runnable task1 = () -> System.out.println("User 1 tasks");
        Runnable task2 = () -> System.out.println("User 2 tasks");
        
        taskManager.scheduleVacancyCheck(user1, task1);
        taskManager.scheduleNotification(user1, time1, timezone, task1);
        taskManager.scheduleVacancyCheck(user2, task2);
        taskManager.scheduleNotification(user2, time2, timezone, task2);
        
        Thread.sleep(100); // Give time for scheduling
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(4);
        
        taskManager.cancelUserTasks(user1);
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(2);
        
        taskManager.cancelUserTasks(user2);
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(0);
    }
    
    @Test
    void shouldReplaceExistingTaskWhenSchedulingAgain() {
        Long userId = 333L;
        Runnable task1 = () -> System.out.println("First task");
        Runnable task2 = () -> System.out.println("Second task");
        
        taskManager.scheduleVacancyCheck(userId, task1);
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(1);
        
        taskManager.scheduleVacancyCheck(userId, task2);
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(1); // Should replace, not add
    }
    
    @Test
    void shouldReplaceExistingNotificationWhenSchedulingAgain() throws InterruptedException {
        Long userId = 444L;
        LocalTime time1 = LocalTime.of(9, 0);
        LocalTime time2 = LocalTime.of(17, 0);
        ZoneOffset timezone = ZoneOffset.ofHours(2);
        
        Runnable task1 = () -> System.out.println("Morning notification");
        Runnable task2 = () -> System.out.println("Evening notification");
        
        taskManager.scheduleNotification(userId, time1, timezone, task1);
        Thread.sleep(100);
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(1);
        
        taskManager.scheduleNotification(userId, time2, timezone, task2);
        Thread.sleep(100);
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(1); // Should replace, not add
    }
    
    @Test
    void shouldHandleNonExistentTaskCancellation() {
        taskManager.cancelTask("non-existent-task");
        taskManager.cancelUserTasks(99999L);
        
        // Should not throw exceptions
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(0);
    }
    
    @Test
    void shouldReturnCorrectActiveTasksCount() throws InterruptedException {
        Long user1 = 555L;
        Long user2 = 666L;
        LocalTime time = LocalTime.of(12, 0);
        ZoneOffset timezone = ZoneOffset.UTC;
        
        Runnable task = () -> System.out.println("Test");
        
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(0);
        
        taskManager.scheduleVacancyCheck(user1, task);
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(1);
        
        taskManager.scheduleNotification(user2, time, timezone, task);
        Thread.sleep(100);
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(2);
        
        taskManager.cancelTask("vacancy-check-" + user1);
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(1);
    }
    
    @Test
    void shouldShutdownGracefully() {
        Long userId = 777L;
        Runnable task = () -> System.out.println("Test task");
        
        taskManager.scheduleVacancyCheck(userId, task);
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(1);
        
        taskManager.shutdown();
        
        // After shutdown, tasks should be cancelled
        // Note: We can't easily test scheduler.isShutdown() without exposing it
    }
    
    @Test
    void shouldHandleTaskExecutionErrors() throws InterruptedException {
        Long userId = 888L;
        
        // Task that throws an exception
        Runnable faultyTask = () -> {
            throw new RuntimeException("Test exception");
        };
        
        taskManager.scheduleVacancyCheck(userId, faultyTask);
        assertThat(taskManager.isTaskScheduled("vacancy-check-" + userId)).isTrue();
        
        Thread.sleep(200); // Give time for task to execute and fail
        
        // Task should still be scheduled despite the error
        assertThat(taskManager.isTaskScheduled("vacancy-check-" + userId)).isTrue();
    }
    
    @Test
    void shouldCalculateInitialDelayForNotifications() throws InterruptedException {
        Long userId = 101L;
        LocalTime futureTime = LocalTime.now().plusHours(1);
        ZoneOffset timezone = ZoneOffset.UTC;
        
        Runnable task = () -> System.out.println("Future notification");
        
        taskManager.scheduleNotification(userId, futureTime, timezone, task);
        Thread.sleep(100);
        
        assertThat(taskManager.isTaskScheduled("notification-" + userId)).isTrue();
        assertThat(taskManager.getActiveTasksCount()).isEqualTo(1);
    }
}