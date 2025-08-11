package com.skillbox.vacancytracker.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UrlBuilder {
    private final StringBuilder baseUrl;
    private final Map<String, String> queryParams;
    
    public UrlBuilder(String baseUrl) {
        this.baseUrl = new StringBuilder(baseUrl);
        this.queryParams = new LinkedHashMap<>();
    }
    
    public UrlBuilder appendPath(String path) {
        if (path != null && !path.isEmpty()) {
            if (!path.startsWith("/")) {
                baseUrl.append("/");
            }
            baseUrl.append(path);
        }
        return this;
    }
    
    public UrlBuilder addParam(String key, String value) {
        if (key != null && value != null) {
            queryParams.put(key, value);
        }
        return this;
    }
    
    public UrlBuilder addParam(String key, Integer value) {
        if (key != null && value != null) {
            queryParams.put(key, value.toString());
        }
        return this;
    }
    
    public UrlBuilder addEncodedParam(String key, String value) {
        if (key != null && value != null && !value.trim().isEmpty()) {
            queryParams.put(key, URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
        return this;
    }
    
    public String build() {
        if (queryParams.isEmpty()) {
            return baseUrl.toString();
        }
        
        String params = queryParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        
        return baseUrl + "?" + params;
    }
}