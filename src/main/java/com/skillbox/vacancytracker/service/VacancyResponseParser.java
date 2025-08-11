package com.skillbox.vacancytracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbox.vacancytracker.exception.VacancyApiException;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.model.Vacancy;
import com.skillbox.vacancytracker.util.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.skillbox.vacancytracker.constant.ApiConstants;

public class VacancyResponseParser {
    private static final Logger logger = LoggerFactory.getLogger(VacancyResponseParser.class);
    
    private final ObjectMapper objectMapper;
    
    public VacancyResponseParser() {
        this.objectMapper = JsonMapper.getInstance();
    }
    
    public List<Vacancy> parseVacancies(String responseBody, SearchCriteria criteria) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        
        String status = root.path(ApiConstants.JSON_STATUS).asText();
        if (!ApiConstants.API_STATUS_OK.equals(status)) {
            throw new VacancyApiException("API returned error status: " + status);
        }
        
        JsonNode results = root.path(ApiConstants.JSON_RESULTS);
        JsonNode vacanciesNode = results.path(ApiConstants.JSON_VACANCIES);
        
        List<Vacancy> vacancies = new ArrayList<>();
        
        if (vacanciesNode.isArray()) {
            for (JsonNode vacancyNode : vacanciesNode) {
                JsonNode vacancy = vacancyNode.path(ApiConstants.JSON_VACANCY);
                if (!vacancy.isMissingNode()) {
                    Vacancy parsedVacancy = parseVacancy(vacancy);
                    if (parsedVacancy != null) {
                        vacancies.add(parsedVacancy);
                    }
                }
            }
        }
        
        logger.debug("Parsed {} vacancies from API response", vacancies.size());
        return filterVacancies(vacancies, criteria);
    }
    
    private Vacancy parseVacancy(JsonNode vacancyNode) {
        try {
            Vacancy vacancy = new Vacancy();
            
            vacancy.setId(vacancyNode.path("id").asText());
            vacancy.setTitle(vacancyNode.path("job-name").asText());
            vacancy.setCompanyName(vacancyNode.path("company").path("name").asText());
            vacancy.setDescription(vacancyNode.path("duty").asText());
            vacancy.setUrl(ApiConstants.API_VACANCY_URL_PREFIX + vacancy.getId());
            
            JsonNode salaryNode = vacancyNode.path("salary");
            if (!salaryNode.isMissingNode()) {
                vacancy.setSalaryFrom(parseInteger(salaryNode.path("from")));
                vacancy.setSalaryTo(parseInteger(salaryNode.path("to")));
                vacancy.setCurrency(salaryNode.path("currency").asText("RUB"));
            }
            
            JsonNode experienceNode = vacancyNode.path("requirement");
            if (!experienceNode.isMissingNode()) {
                vacancy.setExperienceRequired(parseInteger(experienceNode.path("experience")));
            }
            
            JsonNode regionNode = vacancyNode.path("region");
            if (!regionNode.isMissingNode()) {
                vacancy.setRegion(regionNode.path("name").asText());
                vacancy.setRegionCode(parseInteger(regionNode.path("code")));
            }
            
            String createdDate = vacancyNode.path("creation-date").asText();
            if (!createdDate.isEmpty()) {
                vacancy.setCreatedDate(LocalDateTime.parse(createdDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
            String modifiedDate = vacancyNode.path("modification-date").asText();
            if (!modifiedDate.isEmpty()) {
                vacancy.setModifiedDate(LocalDateTime.parse(modifiedDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
            return vacancy;
            
        } catch (Exception e) {
            logger.warn("Failed to parse vacancy: {}", e.getMessage());
            return null;
        }
    }
    
    private Integer parseInteger(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        try {
            return node.asInt();
        } catch (Exception e) {
            return null;
        }
    }
    
    private List<Vacancy> filterVacancies(List<Vacancy> vacancies, SearchCriteria criteria) {
        return vacancies.stream()
                .filter(vacancy -> matchesSalaryCriteria(vacancy, criteria))
                .toList();
    }
    
    private boolean matchesSalaryCriteria(Vacancy vacancy, SearchCriteria criteria) {
        if (criteria.getMinimumSalary() == null) {
            return true;
        }
        
        Integer salaryFrom = vacancy.getSalaryFrom();
        Integer salaryTo = vacancy.getSalaryTo();
        
        if (salaryFrom != null && salaryFrom >= criteria.getMinimumSalary()) {
            return true;
        }
        
        if (salaryTo != null && salaryTo >= criteria.getMinimumSalary()) {
            return true;
        }
        
        return salaryFrom == null && salaryTo == null;
    }
}