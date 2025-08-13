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
                if (!vacancy.isMissingNode() && !vacancy.isNull()) {
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
            vacancy.setTitle(parseTextOrNull(vacancyNode.path("job-name")));
            vacancy.setCompanyName(parseTextOrNull(vacancyNode.path("company").path("name")));
            vacancy.setDescription(parseTextOrNull(vacancyNode.path("duty")));
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
            if (!regionNode.isMissingNode() && !regionNode.isNull()) {
                vacancy.setRegion(parseTextOrNull(regionNode.path("name")));
                vacancy.setRegionCode(parseInteger(regionNode.path("code")));
            }
            
            String createdDate = vacancyNode.path("creation-date").asText();
            if (!createdDate.isEmpty()) {
                try {
                    vacancy.setCreatedDate(LocalDateTime.parse(createdDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } catch (Exception e) {
                    logger.debug("Failed to parse creation date: {}", createdDate);
                    vacancy.setCreatedDate(null);
                }
            }
            
            String modifiedDate = vacancyNode.path("modification-date").asText();
            if (!modifiedDate.isEmpty()) {
                try {
                    vacancy.setModifiedDate(LocalDateTime.parse(modifiedDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } catch (Exception e) {
                    logger.debug("Failed to parse modification date: {}", modifiedDate);
                    vacancy.setModifiedDate(null);
                }
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
        if (!node.isNumber() && !node.isTextual()) {
            return null;
        }
        try {
            if (node.isNumber()) {
                return node.asInt();
            } else {
                String text = node.asText();
                return Integer.parseInt(text);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private String parseTextOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String text = node.asText();
        return text.isEmpty() ? null : text;
    }
    
    private List<Vacancy> filterVacancies(List<Vacancy> vacancies, SearchCriteria criteria) {
        return vacancies.stream()
                .filter(vacancy -> matchesSalaryCriteria(vacancy, criteria))
                .filter(vacancy -> matchesKeywordCriteria(vacancy, criteria))
                .filter(vacancy -> matchesExperienceCriteria(vacancy, criteria))
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
    
    private boolean matchesKeywordCriteria(Vacancy vacancy, SearchCriteria criteria) {
        if (criteria.getKeyword() == null || criteria.getKeyword().isEmpty()) {
            return true;
        }
        
        String keyword = criteria.getKeyword().toLowerCase();
        String title = vacancy.getTitle() != null ? vacancy.getTitle().toLowerCase() : "";
        String company = vacancy.getCompanyName() != null ? vacancy.getCompanyName().toLowerCase() : "";
        String description = vacancy.getDescription() != null ? vacancy.getDescription().toLowerCase() : "";
        
        return title.contains(keyword) || company.contains(keyword) || description.contains(keyword);
    }
    
    private boolean matchesExperienceCriteria(Vacancy vacancy, SearchCriteria criteria) {
        if (criteria.getMinimumExperience() == null) {
            return true;
        }
        
        Integer experience = vacancy.getExperienceRequired();
        if (experience == null) {
            return true;
        }
        
        return experience >= criteria.getMinimumExperience();
    }
}