package com.skillbox.vacancytracker.task;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.model.UserVacancy;
import com.skillbox.vacancytracker.model.Vacancy;
import com.skillbox.vacancytracker.repository.UserVacancyRepository;
import com.skillbox.vacancytracker.service.VacancyApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VacancyCheckTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(VacancyCheckTask.class);
    
    private final BotUser user;
    private final VacancyApiClient vacancyApiClient;
    private final UserVacancyRepository userVacancyRepository;
    
    public VacancyCheckTask(BotUser user, 
                           VacancyApiClient vacancyApiClient,
                           UserVacancyRepository userVacancyRepository) {
        this.user = user;
        this.vacancyApiClient = vacancyApiClient;
        this.userVacancyRepository = userVacancyRepository;
    }
    
    @Override
    public void run() {
        if (!user.isActive()) {
            logger.debug("User {} is not active, skipping vacancy check", user.getUserId());
            return;
        }
        
        SearchCriteria criteria = user.getSearchCriteria();
        if (criteria == null || criteria.isEmpty()) {
            logger.debug("User {} has no search criteria, skipping", user.getUserId());
            return;
        }
        
        try {
            logger.info("Checking vacancies for user {}", user.getUserId());
            
            List<UserVacancy> existingVacancies = userVacancyRepository.findByUserId(user.getUserId());
            Set<String> existingVacancyIds = existingVacancies.stream()
                .map(UserVacancy::getVacancyId)
                .collect(Collectors.toSet());
            
            List<Vacancy> currentVacancies = vacancyApiClient.searchVacancies(criteria);
            
            List<UserVacancy> newUserVacancies = currentVacancies.stream()
                .filter(v -> !existingVacancyIds.contains(v.getId()))
                .map(v -> new UserVacancy(user.getUserId(), v))
                .toList();
            
            if (!newUserVacancies.isEmpty()) {
                logger.info("Found {} new vacancies for user {}", 
                           newUserVacancies.size(), user.getUserId());
                
                for (UserVacancy userVacancy : newUserVacancies) {
                    userVacancyRepository.save(userVacancy);
                }
            } else {
                logger.debug("No new vacancies found for user {}", user.getUserId());
            }
            
        } catch (Exception e) {
            logger.error("Error checking vacancies for user {}", user.getUserId(), e);
        }
    }
}