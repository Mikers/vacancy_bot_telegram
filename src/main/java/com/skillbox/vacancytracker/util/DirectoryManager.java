package com.skillbox.vacancytracker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DirectoryManager {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryManager.class);
    
    private DirectoryManager() {
    }
    
    public static void initializeDirectories(String dataDirectory) throws IOException {
        createDataDirectory(dataDirectory);
        createLogsDirectory();
    }
    
    private static void createDataDirectory(String dataDirectory) throws IOException {
        Path dataPath = Path.of(dataDirectory);
        if (!Files.exists(dataPath)) {
            Files.createDirectories(dataPath);
            logger.info("Created data directory: {}", dataPath.toAbsolutePath());
        } else {
            logger.debug("Data directory already exists: {}", dataPath.toAbsolutePath());
        }
    }
    
    private static void createLogsDirectory() throws IOException {
        Path logsPath = Path.of("logs");
        if (!Files.exists(logsPath)) {
            Files.createDirectories(logsPath);
            logger.info("Created logs directory: {}", logsPath.toAbsolutePath());
        } else {
            logger.debug("Logs directory already exists: {}", logsPath.toAbsolutePath());
        }
    }
}