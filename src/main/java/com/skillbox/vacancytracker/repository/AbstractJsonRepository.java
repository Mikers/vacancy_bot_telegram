package com.skillbox.vacancytracker.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbox.vacancytracker.exception.RepositoryException;
import com.skillbox.vacancytracker.util.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractJsonRepository<T, ID> implements Repository<T, ID> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractJsonRepository.class);
    
    protected final ObjectMapper objectMapper;
    protected final File dataFile;
    protected final TypeReference<Map<String, T>> typeReference;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    protected AbstractJsonRepository(String dataDirectory, String fileName, TypeReference<Map<String, T>> typeReference) {
        this.objectMapper = JsonMapper.getInstance();
        this.typeReference = typeReference;
        
        try {
            Path dataPath = Path.of(dataDirectory);
            Files.createDirectories(dataPath);
            this.dataFile = dataPath.resolve(fileName).toFile();
            
            if (!dataFile.exists()) {
                saveDataToFile(new HashMap<>());
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to initialize repository: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<T> findById(ID id) {
        lock.readLock().lock();
        try {
            Map<String, T> data = loadDataFromFile();
            T entity = data.get(String.valueOf(id));
            return Optional.ofNullable(entity);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public boolean exists(ID id) {
        return findById(id).isPresent();
    }
    
    @Override
    public void save(T entity) {
        lock.writeLock().lock();
        try {
            Map<String, T> data = loadDataFromFile();
            data.put(String.valueOf(getId(entity)), entity);
            saveDataToFile(data);
            logger.debug("Entity saved: {}", entity);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void delete(ID id) {
        lock.writeLock().lock();
        try {
            Map<String, T> data = loadDataFromFile();
            T removed = data.remove(String.valueOf(id));
            if (removed != null) {
                saveDataToFile(data);
                logger.debug("Entity deleted with id: {}", id);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public List<T> findAll() {
        lock.readLock().lock();
        try {
            Map<String, T> data = loadDataFromFile();
            return List.copyOf(data.values());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    protected abstract ID getId(T entity);
    
    private Map<String, T> loadDataFromFile() {
        try {
            if (!dataFile.exists() || dataFile.length() == 0) {
                return new HashMap<>();
            }
            return objectMapper.readValue(dataFile, typeReference);
        } catch (IOException e) {
            logger.error("Failed to load data from file: {}", dataFile.getAbsolutePath(), e);
            throw new RepositoryException("Failed to load data from file: " + e.getMessage(), e);
        }
    }
    
    private void saveDataToFile(Map<String, T> data) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, data);
        } catch (IOException e) {
            logger.error("Failed to save data to file: {}", dataFile.getAbsolutePath(), e);
            throw new RepositoryException("Failed to save data to file: " + e.getMessage(), e);
        }
    }
}