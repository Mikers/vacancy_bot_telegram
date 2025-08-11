package com.skillbox.vacancytracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.ZoneOffset;
import java.util.Objects;

public class BotUser {
    @JsonProperty("user_id")
    @NotNull
    private Long userId;
    
    @JsonProperty("chat_id")
    @NotNull
    private Long chatId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    @JsonProperty("timezone_offset")
    private ZoneOffset timezoneOffset;
    
    @JsonProperty("search_criteria")
    private SearchCriteria searchCriteria;
    
    @JsonProperty("notification_time")
    private String notificationTime; // HH:mm format
    
    @JsonProperty("is_active")
    private boolean active;
    
    public BotUser() {
        this.timezoneOffset = ZoneOffset.UTC;
        this.active = true;
        this.searchCriteria = new SearchCriteria();
    }
    
    public BotUser(Long userId, Long chatId) {
        this();
        this.userId = userId;
        this.chatId = chatId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getChatId() {
        return chatId;
    }
    
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public ZoneOffset getTimezoneOffset() {
        return timezoneOffset;
    }
    
    public void setTimezoneOffset(ZoneOffset timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }
    
    public SearchCriteria getSearchCriteria() {
        return searchCriteria;
    }
    
    public void setSearchCriteria(SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }
    
    public String getNotificationTime() {
        return notificationTime;
    }
    
    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotUser botUser = (BotUser) o;
        return Objects.equals(userId, botUser.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
    
    @Override
    public String toString() {
        return "BotUser{" +
                "userId=" + userId +
                ", chatId=" + chatId +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", active=" + active +
                '}';
    }
}