package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.model.Vacancy;

import java.util.List;

public interface VacancyApiClient {
    List<Vacancy> searchVacancies(SearchCriteria criteria);
    
    List<Vacancy> searchVacancies(SearchCriteria criteria, int limit, int offset);
}