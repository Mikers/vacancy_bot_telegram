package com.skillbox.vacancytracker.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.skillbox.vacancytracker.model.UserVacancy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonUserVacancyRepository extends AbstractJsonRepository<UserVacancy, String> 
        implements UserVacancyRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonUserVacancyRepository.class);
    
    public JsonUserVacancyRepository(String dataDirectory) {
        super(dataDirectory, "user_vacancies", new TypeReference<Map<String, UserVacancy>>() {});
    }
    
    @Override
    protected String getId(UserVacancy entity) {
        return entity.getId();
    }
    
    
    @Override
    public List<UserVacancy> findByUserId(Long userId) {
        return findAll().stream()
                .filter(uv -> userId.equals(uv.getUserId()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserVacancy> findNewByUserId(Long userId) {
        return findByUserId(userId).stream()
                .filter(UserVacancy::isNew)
                .collect(Collectors.toList());
    }
    
    @Override
    public void markAsNotified(String id) {
        findById(id).ifPresent(userVacancy -> {
            userVacancy.setNotifiedAt(LocalDateTime.now());
            userVacancy.setNew(false);
            save(userVacancy);
        });
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        List<UserVacancy> userVacancies = findByUserId(userId);
        for (UserVacancy uv : userVacancies) {
            delete(uv.getId());
        }
        logger.info("Deleted {} vacancies for user {}", userVacancies.size(), userId);
    }
}