package com.skillbox.vacancytracker.model;

import java.time.LocalTime;
import java.time.ZoneOffset;

public class NotificationSettings {
    private LocalTime notificationTime;
    private ZoneOffset userTimezone;
    private boolean enabled;
    
    public NotificationSettings() {
        this.notificationTime = LocalTime.of(9, 0);
        this.userTimezone = ZoneOffset.UTC;
        this.enabled = false;
    }
    
    public NotificationSettings(LocalTime notificationTime, ZoneOffset userTimezone) {
        this.notificationTime = notificationTime;
        this.userTimezone = userTimezone;
        this.enabled = true;
    }
    
    public LocalTime getNotificationTime() {
        return notificationTime;
    }
    
    public void setNotificationTime(LocalTime notificationTime) {
        this.notificationTime = notificationTime;
    }
    
    public ZoneOffset getUserTimezone() {
        return userTimezone;
    }
    
    public void setUserTimezone(ZoneOffset userTimezone) {
        this.userTimezone = userTimezone;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}