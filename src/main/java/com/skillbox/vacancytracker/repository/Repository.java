package com.skillbox.vacancytracker.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    Optional<T> findById(ID id);
    
    boolean exists(ID id);
    
    void save(T entity);
    
    void delete(ID id);
    
    List<T> findAll();
}