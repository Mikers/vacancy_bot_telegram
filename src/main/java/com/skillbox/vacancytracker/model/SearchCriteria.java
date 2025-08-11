package com.skillbox.vacancytracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class SearchCriteria {
    @JsonProperty("region_code")
    private Integer regionCode;
    
    @JsonProperty("minimum_experience")
    private Integer minimumExperience; // years
    
    @JsonProperty("minimum_salary")
    private Integer minimumSalary; // rubles
    
    @JsonProperty("keyword")
    private String keyword;
    
    public SearchCriteria() {
        // Default values as per specification
    }
    
    public Integer getRegionCode() {
        return regionCode;
    }
    
    public void setRegionCode(Integer regionCode) {
        this.regionCode = regionCode;
    }
    
    public Integer getMinimumExperience() {
        return minimumExperience;
    }
    
    public void setMinimumExperience(Integer minimumExperience) {
        this.minimumExperience = minimumExperience;
    }
    
    public Integer getMinimumSalary() {
        return minimumSalary;
    }
    
    public void setMinimumSalary(Integer minimumSalary) {
        this.minimumSalary = minimumSalary;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public boolean isEmpty() {
        return regionCode == null && minimumExperience == null && 
               minimumSalary == null && (keyword == null || keyword.trim().isEmpty());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchCriteria that = (SearchCriteria) o;
        return Objects.equals(regionCode, that.regionCode) &&
                Objects.equals(minimumExperience, that.minimumExperience) &&
                Objects.equals(minimumSalary, that.minimumSalary) &&
                Objects.equals(keyword, that.keyword);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(regionCode, minimumExperience, minimumSalary, keyword);
    }
    
    @Override
    public String toString() {
        return "SearchCriteria{" +
                "regionCode=" + regionCode +
                ", minimumExperience=" + minimumExperience +
                ", minimumSalary=" + minimumSalary +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}