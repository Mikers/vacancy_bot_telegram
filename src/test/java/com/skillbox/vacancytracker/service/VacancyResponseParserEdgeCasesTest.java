package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.exception.VacancyApiException;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.model.Vacancy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class VacancyResponseParserEdgeCasesTest {
    
    private VacancyResponseParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new VacancyResponseParser();
    }
    
    @Test
    void shouldHandlePartialVacancyData() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "123",
                                "job-name": "Partial Job",
                                "company": null,
                                "salary": {"from": 50000},
                                "requirement": null,
                                "region": {"name": "Test City"},
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
        assertThat(vacancy.getId()).isEqualTo("123");
        assertThat(vacancy.getTitle()).isEqualTo("Partial Job");
        assertThat(vacancy.getCompanyName()).isNull();
        assertThat(vacancy.getSalaryFrom()).isEqualTo(50000);
        assertThat(vacancy.getSalaryTo()).isNull();
        assertThat(vacancy.getExperienceRequired()).isNull();
        assertThat(vacancy.getRegion()).isEqualTo("Test City");
        assertThat(vacancy.getDescription()).isNull();
    }
    
    @Test
    void shouldHandleInvalidSalaryValues() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "456",
                                "job-name": "Test Job",
                                "salary": {
                                    "from": "invalid-number",
                                    "to": null,
                                    "currency": "USD"
                                }
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
        assertThat(vacancy.getSalaryFrom()).isNull();
        assertThat(vacancy.getSalaryTo()).isNull();
        assertThat(vacancy.getCurrency()).isEqualTo("USD");
    }
    
    @Test
    void shouldHandleInvalidExperienceValue() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "789",
                                "job-name": "Experience Test",
                                "requirement": {
                                    "experience": "not-a-number"
                                }
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(1);
        assertThat(vacancies.get(0).getExperienceRequired()).isNull();
    }
    
    @Test
    void shouldHandleInvalidRegionCode() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "101",
                                "job-name": "Region Test",
                                "region": {
                                    "name": "Test Region",
                                    "code": "not-a-number"
                                }
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(1);
        assertThat(vacancies.get(0).getRegion()).isEqualTo("Test Region");
        assertThat(vacancies.get(0).getRegionCode()).isNull();
    }
    
    @Test
    void shouldHandleCorruptedVacancyData() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "valid-1",
                                "job-name": "Valid Job"
                            }
                        },
                        {
                            "vacancy": null
                        },
                        {
                            "corrupted": "data"
                        },
                        {
                            "vacancy": {
                                "id": "valid-2",
                                "job-name": "Another Valid Job"
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(2);
        assertThat(vacancies).extracting(Vacancy::getId)
            .containsExactlyInAnyOrder("valid-1", "valid-2");
    }
    
    @Test
    void shouldHandleEmptyStringValues() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "empty-test",
                                "job-name": "",
                                "company": {"name": ""},
                                "duty": "",
                                "creation-date": "",
                                "modification-date": ""
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
        assertThat(vacancy.getTitle()).isNull();
        assertThat(vacancy.getCompanyName()).isNull();
        assertThat(vacancy.getDescription()).isNull();
        assertThat(vacancy.getCreatedDate()).isNull();
        assertThat(vacancy.getModifiedDate()).isNull();
    }
    
    @Test
    void shouldHandleInvalidDateFormats() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "date-test",
                                "job-name": "Date Test Job",
                                "creation-date": "invalid-date-format",
                                "modification-date": "2024-13-45T25:70:99"
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
        assertThat(vacancy.getCreatedDate()).isNull();
        assertThat(vacancy.getModifiedDate()).isNull();
    }
    
    @Test
    void shouldFilterByKeywordInDescription() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "1",
                                "job-name": "Backend Developer",
                                "company": {"name": "Tech Corp"},
                                "duty": "Develop Java applications"
                            }
                        },
                        {
                            "vacancy": {
                                "id": "2",
                                "job-name": "Frontend Developer",
                                "company": {"name": "Web Solutions"},
                                "duty": "Create React components"
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("java");
        
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(1);
        assertThat(vacancies.get(0).getId()).isEqualTo("1");
    }
    
    @Test
    void shouldFilterByKeywordInCompanyName() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "1",
                                "job-name": "Developer",
                                "company": {"name": "Google Inc"},
                                "duty": "Software development"
                            }
                        },
                        {
                            "vacancy": {
                                "id": "2",
                                "job-name": "Developer",
                                "company": {"name": "Microsoft Corp"},
                                "duty": "Software development"
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("google");
        
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).hasSize(1);
        assertThat(vacancies.get(0).getId()).isEqualTo("1");
    }
    
    @Test
    void shouldReturnEmptyForNoMatches() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "1",
                                "job-name": "Python Developer",
                                "salary": {"from": 40000},
                                "requirement": {"experience": 1}
                            }
                        }
                    ]
                }
            }
            """;
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Java");
        criteria.setMinimumSalary(100000);
        criteria.setMinimumExperience(5);
        
        List<Vacancy> vacancies = parser.parseVacancies(json, criteria);
        
        assertThat(vacancies).isEmpty();
    }
    
    @Test
    void shouldHandleVacancyWithMissingId() throws IOException {
        String json = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "job-name": "Job Without ID",
                                "company": {"name": "Test Company"}
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
        assertThat(vacancy.getId()).isEmpty();
        assertThat(vacancy.getUrl()).isEqualTo("https://trudvsem.ru/vacancy/");
    }
}