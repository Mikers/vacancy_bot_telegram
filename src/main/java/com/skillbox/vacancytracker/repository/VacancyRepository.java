package com.skillbox.vacancytracker.repository;

import com.skillbox.vacancytracker.model.Vacancy;

import java.time.LocalDateTime;
import java.util.List;

public interface VacancyRepository extends Repository<Vacancy, String> {
    List<Vacancy> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
    
    List<Vacancy> findByUserIdAndCreatedAfter(Long userId, LocalDateTime after);
    
    void saveUserVacancies(Long userId, List<Vacancy> vacancies);
}