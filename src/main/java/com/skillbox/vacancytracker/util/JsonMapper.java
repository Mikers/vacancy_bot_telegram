package com.skillbox.vacancytracker.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonMapper {
    private static final ObjectMapper INSTANCE;
    
    static {
        INSTANCE = new ObjectMapper();
        INSTANCE.registerModule(new JavaTimeModule());
        INSTANCE.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        INSTANCE.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    private JsonMapper() {
    }
    
    public static ObjectMapper getInstance() {
        return INSTANCE;
    }
}