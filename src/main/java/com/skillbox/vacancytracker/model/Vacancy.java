package com.skillbox.vacancytracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

public class Vacancy {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("company_name")
    private String companyName;
    
    @JsonProperty("salary_from")
    private Integer salaryFrom;
    
    @JsonProperty("salary_to")
    private Integer salaryTo;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("experience_required")
    private Integer experienceRequired;
    
    @JsonProperty("region")
    private String region;
    
    @JsonProperty("region_code")
    private Integer regionCode;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    
    @JsonProperty("modified_date")
    private LocalDateTime modifiedDate;
    
    public Vacancy() {}
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public Integer getSalaryFrom() {
        return salaryFrom;
    }
    
    public void setSalaryFrom(Integer salaryFrom) {
        this.salaryFrom = salaryFrom;
    }
    
    public Integer getSalaryTo() {
        return salaryTo;
    }
    
    public void setSalaryTo(Integer salaryTo) {
        this.salaryTo = salaryTo;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Integer getExperienceRequired() {
        return experienceRequired;
    }
    
    public void setExperienceRequired(Integer experienceRequired) {
        this.experienceRequired = experienceRequired;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public Integer getRegionCode() {
        return regionCode;
    }
    
    public void setRegionCode(Integer regionCode) {
        this.regionCode = regionCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }
    
    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    public String getFormattedSalary() {
        if (salaryFrom == null && salaryTo == null) {
            return "Не указана";
        }
        
        StringBuilder sb = new StringBuilder();
        if (salaryFrom != null && salaryTo != null) {
            sb.append(salaryFrom).append("-").append(salaryTo);
        } else if (salaryFrom != null) {
            sb.append("От ").append(salaryFrom);
        } else {
            sb.append("До ").append(salaryTo);
        }
        
        if (currency != null && !currency.isEmpty()) {
            sb.append(" ").append(currency);
        } else {
            sb.append(" руб.");
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vacancy vacancy = (Vacancy) o;
        return Objects.equals(id, vacancy.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    @Override
    public String toString() {
        return "Vacancy{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", companyName='" + companyName + '\'' +
                ", salaryFrom=" + salaryFrom +
                ", salaryTo=" + salaryTo +
                ", experienceRequired=" + experienceRequired +
                ", region='" + region + '\'' +
                '}';
    }
}