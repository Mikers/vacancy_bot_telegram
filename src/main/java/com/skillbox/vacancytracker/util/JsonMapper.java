package com.skillbox.vacancytracker.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonMapper {
    private static final ObjectMapper INSTANCE;
    
    static {
        INSTANCE = new ObjectMapper();
        INSTANCE.registerModule(new JavaTimeModule());
    }
    
    private JsonMapper() {
    }
    
    public static ObjectMapper getInstance() {
        return INSTANCE;
    }
}