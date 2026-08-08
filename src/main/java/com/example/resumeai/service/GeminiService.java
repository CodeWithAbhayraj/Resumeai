package com.example.resumeai.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client client;

    @Value("${gemini.model}")
    private String model;

    public GeminiService(Client client) {
        this.client = client;
    }

    public String askGemini(String prompt) {

        try {

            GenerateContentResponse response =
                    client.models.generateContent(
                            model,
                            prompt,
                            null
                    );

            String result = response.text();

            if (result == null || result.isBlank()) {
                return "No response received from Gemini.";
            }

            // Remove Markdown if Gemini returns ```json ... ```
            result = result
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error while calling Gemini API: " + e.getMessage(), e);
        }
    }
}