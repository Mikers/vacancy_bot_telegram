package com.skillbox.vacancytracker.constant;

public final class ApiConstants {
    public static final String API_STATUS_OK = "200";
    public static final String API_PATH_REGION = "/region/";
    public static final String API_VACANCY_URL_PREFIX = "https://trudvsem.ru/vacancy/";
    
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_OFFSET = "offset";
    public static final String PARAM_EXPERIENCE_FROM = "experienceFrom";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_MODIFIED_FROM = "modifiedFrom";
    
    public static final String JSON_STATUS = "status";
    public static final String JSON_RESULTS = "results";
    public static final String JSON_VACANCIES = "vacancies";
    public static final String JSON_VACANCY = "vacancy";
    
    public static final int DEFAULT_LIMIT = 100;
    public static final int MAX_API_LIMIT = 100;
    
    private ApiConstants() {
    }
}