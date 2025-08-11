package com.skillbox.vacancytracker.repository;

import com.skillbox.vacancytracker.model.UserVacancy;

import java.util.List;

public interface UserVacancyRepository extends Repository<UserVacancy, String> {
    List<UserVacancy> findByUserId(Long userId);
    List<UserVacancy> findNewByUserId(Long userId);
    void markAsNotified(String id);
    void deleteByUserId(Long userId);
}