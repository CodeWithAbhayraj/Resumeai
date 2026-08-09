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

            String message = e.getMessage();

            // Gemini API quota / rate limit
            if (message != null && message.contains("429")) {

                return """
                        {
                          "error": "Gemini API quota exceeded",
                          "message": "Gemini free-tier request limit has been exceeded. Please wait for the quota to reset or use a project with available Gemini API quota."
                        }
                        """;
            }

            // Other Gemini errors
            throw new RuntimeException(
                    "Error while calling Gemini API: " + message,
                    e
            );
        }
    }
}