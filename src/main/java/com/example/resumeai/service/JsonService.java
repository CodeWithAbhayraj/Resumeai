package com.example.resumeai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class JsonService {

    private final ObjectMapper objectMapper;

    public JsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Object -> JSON
    public String toJson(Object object) {

        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Unable to convert object to JSON.", e);
        }

    }

    // JSON -> Object
    public <T> T fromJson(String json, Class<T> clazz) {

        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse AI response.", e);
        }

    }

}