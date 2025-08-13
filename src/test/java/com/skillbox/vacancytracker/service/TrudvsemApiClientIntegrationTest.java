package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.config.BotConfig;
import com.skillbox.vacancytracker.exception.VacancyApiException;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.model.Vacancy;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TrudvsemApiClientIntegrationTest {
    
    private MockWebServer mockWebServer;
    private TrudvsemApiClient apiClient;
    private BotConfig config;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        config = new BotConfig(
            "test-bot-token",
            "TestBot",
            mockWebServer.url("/").toString(),
            "test-data"
        );
        
        apiClient = new TrudvsemApiClient(config);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    void shouldExecuteSuccessfulRequest() {
        String validJsonResponse = """
            {
                "status": "200",
                "results": {
                    "vacancies": [
                        {
                            "vacancy": {
                                "id": "12345",
                                "job-name": "Java Developer",
                                "company": {"name": "Test Company"},
                                "salary": {"from": 100000, "to": 150000, "currency": "RUB"},
                                "requirement": {"experience": 3},
                                "region": {"name": "Moscow", "code": 77},
                                "duty": "Develop Java applications",
                                "creation-date": "2024-01-15T10:30:00"
                            }
                        }
                    ]
                }
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(validJsonResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Java");
        
        List<Vacancy> vacancies = apiClient.searchVacancies(criteria, 0, 10);
        
        assertThat(vacancies).hasSize(1);
        assertThat(vacancies.get(0).getId()).isEqualTo("12345");
        assertThat(vacancies.get(0).getTitle()).isEqualTo("Java Developer");
    }
    
    @Test
    void shouldExecuteDefaultSearchVacanciesMethod() {
        String validJsonResponse = """
            {
                "status": "200",
                "results": {
                    "vacancies": []
                }
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(validJsonResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));
        
        SearchCriteria criteria = new SearchCriteria();
        List<Vacancy> vacancies = apiClient.searchVacancies(criteria);
        
        assertThat(vacancies).isEmpty();
    }
    
    @Test
    void shouldHandleHttpError() {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("Internal Server Error"));
        
        SearchCriteria criteria = new SearchCriteria();
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria, 0, 10))
            .isInstanceOf(VacancyApiException.class)
            .hasMessageContaining("API request failed with code: 500");
    }
    
    @Test
    void shouldHandleNetworkTimeout() {
        mockWebServer.enqueue(new MockResponse()
            .setBody("timeout response")
            .setBodyDelay(35, java.util.concurrent.TimeUnit.SECONDS)); // Longer than 30s timeout
        
        SearchCriteria criteria = new SearchCriteria();
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria, 0, 10))
            .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldHandleIOException() throws IOException {
        mockWebServer.shutdown();
        
        SearchCriteria criteria = new SearchCriteria();
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria, 0, 10))
            .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldHandleInvalidJsonResponse() {
        mockWebServer.enqueue(new MockResponse()
            .setBody("invalid json content")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));
        
        SearchCriteria criteria = new SearchCriteria();
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria, 0, 10))
            .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldHandleApiErrorStatus() {
        String errorResponse = """
            {
                "status": "400",
                "error": "Bad Request"
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(errorResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));
        
        SearchCriteria criteria = new SearchCriteria();
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria, 0, 10))
            .isInstanceOf(VacancyApiException.class)
            .hasMessageContaining("API returned error status: 400");
    }
    
    @Test
    void shouldBuildUrlWithAllParameters() {
        String validJsonResponse = """
            {
                "status": "200",
                "results": {
                    "vacancies": []
                }
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(validJsonResponse)
            .setResponseCode(200));
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("Java Developer");
        criteria.setMinimumExperience(3);
        criteria.setRegionCode(77);
        
        List<Vacancy> vacancies = apiClient.searchVacancies(criteria, 10, 20);
        
        assertThat(vacancies).isEmpty();
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }
    
    @Test
    void shouldHandleSpecialCharactersInKeyword() {
        String validJsonResponse = """
            {
                "status": "200",
                "results": {
                    "vacancies": []
                }
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(validJsonResponse)
            .setResponseCode(200));
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("C++ Developer & Team Lead");
        
        List<Vacancy> vacancies = apiClient.searchVacancies(criteria, 0, 5);
        
        assertThat(vacancies).isEmpty();
    }
    
    @Test
    void shouldHandleEmptyResponse() {
        mockWebServer.enqueue(new MockResponse()
            .setBody("")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));
        
        SearchCriteria criteria = new SearchCriteria();
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria, 0, 10))
            .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldCreateHttpClient() {
        TrudvsemApiClient newClient = new TrudvsemApiClient(config);
        assertThat(newClient).isNotNull();
    }
}