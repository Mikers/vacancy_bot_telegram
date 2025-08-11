package com.skillbox.vacancytracker.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.skillbox.vacancytracker.model.Vacancy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonVacancyRepository extends AbstractJsonRepository<Vacancy, String> implements VacancyRepository {
    private static final String VACANCY_DATA_FILE = "vacancies.json";
    
    public JsonVacancyRepository(String dataDirectory) {
        super(dataDirectory, VACANCY_DATA_FILE, new TypeReference<Map<String, Vacancy>>() {});
    }
    
    @Override
    protected String getId(Vacancy entity) {
        return entity.getId();
    }
    
    @Override
    public List<Vacancy> findByUserId(Long userId) {
        // In this implementation, we store vacancies with composite key: userId_vacancyId
        lock.readLock().lock();
        try {
            return findAll().stream()
                    .filter(vacancy -> vacancy.getId().startsWith(userId + "_"))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        lock.writeLock().lock();
        try {
            List<Vacancy> userVacancies = findByUserId(userId);
            for (Vacancy vacancy : userVacancies) {
                delete(vacancy.getId());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public List<Vacancy> findByUserIdAndCreatedAfter(Long userId, LocalDateTime after) {
        return findByUserId(userId).stream()
                .filter(vacancy -> vacancy.getCreatedDate() != null && vacancy.getCreatedDate().isAfter(after))
                .collect(Collectors.toList());
    }
    
    @Override
    public void saveUserVacancies(Long userId, List<Vacancy> vacancies) {
        lock.writeLock().lock();
        try {
            // First delete existing vacancies for the user
            deleteByUserId(userId);
            
            // Then save new vacancies with composite keys
            for (int i = 0; i < vacancies.size(); i++) {
                Vacancy vacancy = vacancies.get(i);
                // Create composite key: userId_originalId or userId_index if no original ID
                String compositeId = userId + "_" + (vacancy.getId() != null ? vacancy.getId() : String.valueOf(i));
                vacancy.setId(compositeId);
                save(vacancy);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}