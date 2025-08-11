package com.skillbox.vacancytracker.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record BotConfig(
    @JsonProperty("bot_token")
    @NotBlank(message = "Bot token is required")
    String botToken,
    
    @JsonProperty("bot_name")
    @NotBlank(message = "Bot name is required")
    String botName,
    
    @JsonProperty("vacancy_api_url")
    @NotBlank(message = "Vacancy API URL is required")
    String vacancyApiUrl,
    
    @JsonProperty("data_directory")
    String dataDirectory
) {
    public BotConfig {
        if (dataDirectory == null || dataDirectory.isBlank()) {
            dataDirectory = "data";
        }
    }
}