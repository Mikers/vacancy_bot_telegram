package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<BotUser> findById(Long userId);
    
    void save(BotUser user);
    
    void delete(Long userId);
    
    List<BotUser> findAllActive();
    
    void updateSearchCriteria(Long userId, SearchCriteria criteria);
    
    void updateTimezone(Long userId, ZoneOffset timezone);
    
    void updateNotificationTime(Long userId, String notificationTime);
    
    void deactivateUser(Long userId);
}