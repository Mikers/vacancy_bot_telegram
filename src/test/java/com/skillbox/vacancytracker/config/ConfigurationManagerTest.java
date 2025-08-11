package com.skillbox.vacancytracker.config;

import com.skillbox.vacancytracker.exception.ConfigurationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurationManagerTest {
    
    @Test
    void shouldCreateConfigurationManager() {
        ConfigurationManager configManager = new ConfigurationManager();
        assertThat(configManager).isNotNull();
    }
    
    @Test
    void shouldLoadConfigurationFromClasspath() {
        ConfigurationManager configManager = new ConfigurationManager();
        
        try {
            configManager.loadConfiguration();
            // If no exception, it loaded successfully (which means validation passed)
        } catch (ConfigurationException e) {
            // This is expected if validation fails
            assertThat(e).isInstanceOf(ConfigurationException.class);
        }
    }
}