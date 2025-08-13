package com.skillbox.vacancytracker.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VacancyApiExceptionTest {
    
    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "API request failed";
        
        VacancyApiException exception = new VacancyApiException(message);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }
    
    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "API parsing error";
        RuntimeException cause = new RuntimeException("JSON parse failed");
        
        VacancyApiException exception = new VacancyApiException(message, cause);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
    
    @Test
    void shouldCreateExceptionWithNullMessage() {
        VacancyApiException exception = new VacancyApiException(null);
        
        assertThat(exception.getMessage()).isNull();
    }
    
    @Test
    void shouldCreateExceptionWithEmptyMessage() {
        String message = "";
        
        VacancyApiException exception = new VacancyApiException(message);
        
        assertThat(exception.getMessage()).isEqualTo(message);
    }
    
    @Test
    void shouldCreateExceptionWithNullCause() {
        String message = "Test message";
        
        VacancyApiException exception = new VacancyApiException(message, null);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }
}