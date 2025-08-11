package com.skillbox.vacancytracker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbox.vacancytracker.exception.ConfigurationException;
import com.skillbox.vacancytracker.util.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private static final String CONFIG_FILE = "application.json";
    private static final String ENV_CONFIG_PATH = "CONFIG_PATH";
    
    private final ObjectMapper objectMapper;
    private final Validator validator;
    
    public ConfigurationManager() {
        this.objectMapper = JsonMapper.getInstance();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    public BotConfig loadConfiguration() {
        try {
            BotConfig config = loadConfigFromFile();
            validateConfiguration(config);
            logger.info("Configuration loaded successfully");
            return config;
        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
            throw new ConfigurationException("Failed to load configuration: " + e.getMessage(), e);
        }
    }
    
    private BotConfig loadConfigFromFile() throws IOException {
        String configPath = System.getenv(ENV_CONFIG_PATH);
        
        if (configPath != null) {
            File configFile = new File(configPath);
            if (configFile.exists()) {
                logger.info("Loading configuration from: {}", configPath);
                return objectMapper.readValue(configFile, BotConfig.class);
            }
        }
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IOException("Configuration file not found: " + CONFIG_FILE);
            }
            logger.info("Loading configuration from classpath: {}", CONFIG_FILE);
            return objectMapper.readValue(inputStream, BotConfig.class);
        }
    }
    
    private void validateConfiguration(BotConfig config) {
        Set<ConstraintViolation<BotConfig>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Configuration validation failed:\n");
            for (ConstraintViolation<BotConfig> violation : violations) {
                sb.append("- ").append(violation.getMessage()).append("\n");
            }
            throw new ConfigurationException(sb.toString());
        }
    }
}