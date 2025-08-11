package com.skillbox.vacancytracker.service;

import com.skillbox.vacancytracker.config.BotConfig;
import com.skillbox.vacancytracker.exception.VacancyApiException;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.model.Vacancy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.skillbox.vacancytracker.constant.ApiConstants;
import com.skillbox.vacancytracker.util.UrlBuilder;

public class TrudvsemApiClient implements VacancyApiClient {
    private static final Logger logger = LoggerFactory.getLogger(TrudvsemApiClient.class);
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    
    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final VacancyResponseParser responseParser;
    
    public TrudvsemApiClient(BotConfig config) {
        this.httpClient = createHttpClient();
        this.baseUrl = config.vacancyApiUrl();
        this.responseParser = new VacancyResponseParser();
        logger.info("TrudvsemApiClient initialized with base URL: {}", baseUrl);
    }
    
    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT)
                .readTimeout(READ_TIMEOUT)
                .build();
    }
    
    @Override
    public List<Vacancy> searchVacancies(SearchCriteria criteria) {
        return searchVacancies(criteria, ApiConstants.DEFAULT_LIMIT, 0);
    }
    
    @Override
    public List<Vacancy> searchVacancies(SearchCriteria criteria, int limit, int offset) {
        try {
            String url = buildSearchUrl(criteria, limit, offset);
            String responseBody = executeRequest(url);
            return responseParser.parseVacancies(responseBody, criteria);
        } catch (IOException e) {
            logger.error("Error making API request", e);
            throw new VacancyApiException("Failed to fetch vacancies: " + e.getMessage(), e);
        }
    }
    
    private String executeRequest(String url) throws IOException {
        logger.debug("Making API request to: {}", url);
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new VacancyApiException("API request failed with code: " + response.code());
            }
            
            if (response.body() == null) {
                throw new VacancyApiException("API returned empty response");
            }
            
            return response.body().string();
        }
    }
    
    private String buildSearchUrl(SearchCriteria criteria, int limit, int offset) {
        UrlBuilder urlBuilder = new UrlBuilder(baseUrl);
        
        if (criteria.getRegionCode() != null) {
            urlBuilder.appendPath(ApiConstants.API_PATH_REGION + criteria.getRegionCode());
        }
        
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        String modifiedFrom = oneDayAgo.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
        
        return urlBuilder
                .addParam(ApiConstants.PARAM_LIMIT, Math.min(limit, ApiConstants.MAX_API_LIMIT))
                .addParam(ApiConstants.PARAM_OFFSET, offset)
                .addParam(ApiConstants.PARAM_EXPERIENCE_FROM, criteria.getMinimumExperience())
                .addEncodedParam(ApiConstants.PARAM_TEXT, criteria.getKeyword())
                .addParam(ApiConstants.PARAM_MODIFIED_FROM, modifiedFrom)
                .build();
    }
}