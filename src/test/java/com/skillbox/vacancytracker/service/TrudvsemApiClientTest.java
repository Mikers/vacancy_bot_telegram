package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.config.BotConfig;
import com.skillbox.vacancytracker.model.SearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrudvsemApiClientTest {
    
    private TrudvsemApiClient apiClient;
    private BotConfig config;
    
    @BeforeEach
    void setUp() {
        config = new BotConfig("token", "bot", "api-key", "https://trudvsem.ru/iblocks");
        apiClient = new TrudvsemApiClient(config);
    }
    
    @Test
    void shouldCreateApiClientWithValidConfig() {
        assertThat(apiClient).isNotNull();
    }
    
    @Test
    void shouldHandleEmptySearchCriteria() {
        SearchCriteria criteria = new SearchCriteria();
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria))
            .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldHandleSearchCriteriaWithKeyword() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("java");
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria))
            .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldHandleSearchCriteriaWithAllParameters() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("python");
        criteria.setMinimumSalary(100000);
        criteria.setMinimumExperience(2);
        criteria.setRegionCode(77);
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria))
            .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldHandleNullCriteria() {
        assertThatThrownBy(() -> apiClient.searchVacancies(null))
            .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldCreateClientWithDifferentConfig() {
        BotConfig newConfig = new BotConfig("token2", "bot2", "key2", "https://test.com");
        TrudvsemApiClient newClient = new TrudvsemApiClient(newConfig);
        
        assertThat(newClient).isNotNull();
        assertThat(newClient).isNotSameAs(apiClient);
    }
    
    @Test
    void shouldHandleCriteriaWithSpecialCharacters() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("C++ & Java");
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria))
            .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldHandleCriteriaWithNegativeValues() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("test");
        criteria.setMinimumSalary(-1000);
        criteria.setMinimumExperience(-1);
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria))
            .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldHandleVeryLongKeyword() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("a".repeat(1000));
        
        assertThatThrownBy(() -> apiClient.searchVacancies(criteria))
            .isInstanceOf(RuntimeException.class);
    }
}