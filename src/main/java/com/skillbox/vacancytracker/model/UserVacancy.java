package com.skillbox.vacancytracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserVacancy {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("vacancy_id")
    private String vacancyId;
    
    @JsonProperty("vacancy")
    private Vacancy vacancy;
    
    @JsonProperty("found_at")
    private LocalDateTime foundAt;
    
    @JsonProperty("notified_at")
    private LocalDateTime notifiedAt;
    
    @JsonProperty("is_new")
    private boolean isNew;
    
    public UserVacancy() {
        this.foundAt = LocalDateTime.now();
        this.isNew = true;
    }
    
    public UserVacancy(Long userId, Vacancy vacancy) {
        this();
        this.userId = userId;
        this.vacancyId = vacancy.getId();
        this.vacancy = vacancy;
        this.id = userId + "-" + vacancyId;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getVacancyId() {
        return vacancyId;
    }
    
    public void setVacancyId(String vacancyId) {
        this.vacancyId = vacancyId;
    }
    
    public Vacancy getVacancy() {
        return vacancy;
    }
    
    public void setVacancy(Vacancy vacancy) {
        this.vacancy = vacancy;
    }
    
    public LocalDateTime getFoundAt() {
        return foundAt;
    }
    
    public void setFoundAt(LocalDateTime foundAt) {
        this.foundAt = foundAt;
    }
    
    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }
    
    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
    
    public boolean isNew() {
        return isNew;
    }
    
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserVacancy that = (UserVacancy) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}