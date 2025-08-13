package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.model.Vacancy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VacancyResponseParserTest {
    
    private VacancyResponseParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new VacancyResponseParser();
    }
    
    @Test
    void shouldParseValidVacancyResponse() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "123",
                                "job-name": "Java Developer",
                                "company": { "name": "Tech Company" },
                                "salary": { "from": 150000, "to": 200000, "currency": "RUB" },
                                "requirement": { "experience": 3 },
                                "region": { "name": "Moscow", "code": 77 },
                                "duty": "Java backend development",
                                "creation-date": "2024-01-15T10:30:00"
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(1);
        
        Vacancy vacancy = vacancies.get(0);
        assertThat(vacancy.getId()).isEqualTo("123");
        assertThat(vacancy.getTitle()).isEqualTo("Java Developer");
        assertThat(vacancy.getCompanyName()).isEqualTo("Tech Company");
        assertThat(vacancy.getUrl()).isEqualTo("https://trudvsem.ru/vacancy/123");
        assertThat(vacancy.getSalaryFrom()).isEqualTo(150000);
        assertThat(vacancy.getSalaryTo()).isEqualTo(200000);
        assertThat(vacancy.getCurrency()).isEqualTo("RUB");
        assertThat(vacancy.getExperienceRequired()).isEqualTo(3);
        assertThat(vacancy.getRegion()).isEqualTo("Moscow");
        assertThat(vacancy.getRegionCode()).isEqualTo(77);
        assertThat(vacancy.getDescription()).isEqualTo("Java backend development");
    }
    
    @Test
    void shouldHandleEmptyVacanciesList() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": []
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).isEmpty();
    }
    
    @Test
    void shouldHandleMissingVacanciesField() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {}
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).isEmpty();
    }
    
    @Test
    void shouldHandleMissingResultsField() throws IOException {
        String json = "{\"status\": \"200\"}";
        
        SearchCriteria criteria = new SearchCriteria();
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).isEmpty();
    }
    
    @Test
    void shouldFilterByKeyword() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "1",
                                "job-name": "Java Developer",
                                "company": { "name": "Tech Co" }
                            }
                        },
                        {
                            "vacancy": {
                                "id": "2",
                                "job-name": "Python Developer",
                                "company": { "name": "Software Inc" }
                            }
                        },
                        {
                            "vacancy": {
                                "id": "3",
                                "job-name": "Senior Java Engineer",
                                "company": { "name": "Big Corp" }
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Java");
        
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(2);
        assertThat(vacancies).extracting(Vacancy::getId).containsExactlyInAnyOrder("1", "3");
    }
    
    @Test
    void shouldFilterBySalary() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "1",
                                "job-name": "Junior Developer",
                                "salary": { "from": 80000 }
                            }
                        },
                        {
                            "vacancy": {
                                "id": "2",
                                "job-name": "Senior Developer",
                                "salary": { "from": 150000 }
                            }
                        },
                        {
                            "vacancy": {
                                "id": "3",
                                "job-name": "Lead Developer",
                                "salary": { "to": 200000 }
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setMinimumSalary(100000);
        
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(2);
        assertThat(vacancies).extracting(Vacancy::getId).containsExactlyInAnyOrder("2", "3");
    }
    
    @Test
    void shouldFilterByExperience() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "1",
                                "job-name": "Junior",
                                "requirement": { "experience": 1 }
                            }
                        },
                        {
                            "vacancy": {
                                "id": "2",
                                "job-name": "Middle",
                                "requirement": { "experience": 3 }
                            }
                        },
                        {
                            "vacancy": {
                                "id": "3",
                                "job-name": "Senior",
                                "requirement": { "experience": 5 }
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setMinimumExperience(3);
        
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(2);
        assertThat(vacancies).extracting(Vacancy::getId).containsExactlyInAnyOrder("2", "3");
    }
    
    @Test
    void shouldHandleNullValues() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "789",
                                "job-name": "Developer",
                                "company": null,
                                "salary": null,
                                "requirement": null,
                                "region": null,
                                "duty": null
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(1);
        
        Vacancy vacancy = vacancies.get(0);
        assertThat(vacancy.getId()).isEqualTo("789");
        assertThat(vacancy.getTitle()).isEqualTo("Developer");
        assertThat(vacancy.getCompanyName()).isNull();
        assertThat(vacancy.getUrl()).isEqualTo("https://trudvsem.ru/vacancy/789");
        assertThat(vacancy.getSalaryFrom()).isNull();
        assertThat(vacancy.getSalaryTo()).isNull();
        assertThat(vacancy.getCurrency()).isEqualTo("RUB");
        assertThat(vacancy.getExperienceRequired()).isNull();
        assertThat(vacancy.getRegion()).isNull();
        assertThat(vacancy.getDescription()).isNull();
    }
    
    @Test
    void shouldThrowExceptionForInvalidJson() {
        String invalidJson = "not a json";
        SearchCriteria criteria = new SearchCriteria();
        
        assertThatThrownBy(() -> parser.parseVacancies(invalidJson, criteria))
                .isInstanceOf(Exception.class);
    }
    
    @Test
    void shouldThrowExceptionForInvalidStatus() {
        String json = """
            {
                "status": "400",
                "results": {
                    "vacancies": []
                }
            }
            """;
        SearchCriteria criteria = new SearchCriteria();
        
        assertThatThrownBy(() -> parser.parseVacancies(json, criteria))
                .hasMessageContaining("API returned error status");
    }
    
    @Test
    void shouldApplyMultipleFilters() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "1",
                                "job-name": "Java Junior",
                                "salary": { "from": 60000 },
                                "requirement": { "experience": 1 }
                            }
                        },
                        {
                            "vacancy": {
                                "id": "2",
                                "job-name": "Java Senior",
                                "salary": { "from": 180000 },
                                "requirement": { "experience": 5 }
                            }
                        },
                        {
                            "vacancy": {
                                "id": "3",
                                "job-name": "Python Developer",
                                "salary": { "from": 120000 },
                                "requirement": { "experience": 3 }
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Java");
        criteria.setMinimumSalary(100000);
        criteria.setMinimumExperience(3);
        
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(1);
        assertThat(vacancies.get(0).getId()).isEqualTo("2");
    }
    
    @Test
    void shouldHandleEmptySearchCriteria() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "1",
                                "job-name": "Developer"
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(1);
        assertThat(vacancies.get(0).getId()).isEqualTo("1");
    }
}