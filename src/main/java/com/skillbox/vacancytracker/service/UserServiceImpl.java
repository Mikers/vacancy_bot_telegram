package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.exception.UserNotFoundException;
import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public Optional<BotUser> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    @Override
    public void save(BotUser user) {
        userRepository.save(user);
        logger.debug("User saved: {}", user.getUserId());
    }
    
    @Override
    public void delete(Long userId) {
        userRepository.delete(userId);
        logger.info("User deleted: {}", userId);
    }
    
    @Override
    public List<BotUser> findAllActive() {
        return userRepository.findAll().stream()
                .filter(BotUser::isActive)
                .toList();
    }
    
    @Override
    public void updateSearchCriteria(Long userId, SearchCriteria criteria) {
        BotUser user = findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        user.setSearchCriteria(criteria);
        save(user);
        logger.debug("Updated search criteria for user {}: {}", userId, criteria);
    }
    
    @Override
    public void updateTimezone(Long userId, ZoneOffset timezone) {
        BotUser user = findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        user.setTimezoneOffset(timezone);
        save(user);
        logger.debug("Updated timezone for user {}: {}", userId, timezone);
    }
    
    @Override
    public void updateNotificationTime(Long userId, String notificationTime) {
        BotUser user = findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        user.setNotificationTime(notificationTime);
        save(user);
        logger.debug("Updated notification time for user {}: {}", userId, notificationTime);
    }
    
    @Override
    public void deactivateUser(Long userId) {
        BotUser user = findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        user.setActive(false);
        save(user);
        logger.info("Deactivated user: {}", userId);
    }
}