package com.skillbox.vacancytracker.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionTest {
    
    @Test
    void shouldCreateUserNotFoundException() {
        String message = "User not found: 123";
        
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldCreateUserNotFoundExceptionWithCause() {
        String message = "User not found: 456";
        Throwable cause = new IllegalArgumentException("Invalid user ID");
        
        UserNotFoundException exception = new UserNotFoundException(message, cause);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
    
    @Test
    void shouldCreateRepositoryException() {
        String message = "Repository operation failed";
        
        RepositoryException exception = new RepositoryException(message);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldCreateRepositoryExceptionWithCause() {
        String message = "Failed to save entity";
        Throwable cause = new IllegalStateException("Database connection failed");
        
        RepositoryException exception = new RepositoryException(message, cause);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
    
    @Test
    void shouldCreateConfigurationException() {
        String message = "Configuration loading failed";
        
        ConfigurationException exception = new ConfigurationException(message);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
    
    @Test
    void shouldCreateConfigurationExceptionWithCause() {
        String message = "Invalid configuration file";
        Throwable cause = new java.io.IOException("File not found");
        
        ConfigurationException exception = new ConfigurationException(message, cause);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
    
    @Test
    void shouldThrowUserNotFoundException() {
        assertThatThrownBy(() -> {
            throw new UserNotFoundException("Test user not found");
        })
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("Test user not found");
    }
    
    @Test
    void shouldThrowRepositoryException() {
        assertThatThrownBy(() -> {
            throw new RepositoryException("Test repository error");
        })
        .isInstanceOf(RepositoryException.class)
        .hasMessage("Test repository error");
    }
    
    @Test
    void shouldThrowConfigurationException() {
        assertThatThrownBy(() -> {
            throw new ConfigurationException("Test configuration error");
        })
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Test configuration error");
    }
}