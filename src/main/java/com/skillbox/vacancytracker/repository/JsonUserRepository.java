package com.skillbox.vacancytracker.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.skillbox.vacancytracker.model.BotUser;

import java.util.Map;

public class JsonUserRepository extends AbstractJsonRepository<BotUser, Long> implements UserRepository {
    private static final String USER_DATA_FILE = "users.json";
    
    public JsonUserRepository(String dataDirectory) {
        super(dataDirectory, USER_DATA_FILE, new TypeReference<Map<String, BotUser>>() {});
    }
    
    @Override
    protected Long getId(BotUser entity) {
        return entity.getUserId();
    }
}