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
                throw new RuntimeException(
                        "No response received from Gemini."
                );
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

                throw new RuntimeException(
                        "Gemini API quota exceeded. " +
                                "Please wait for quota reset or use a Gemini API project " +
                                "with available quota."
                );
            }

            // Other Gemini errors
            throw new RuntimeException(
                    "Error while calling Gemini API: " + message,
                    e
            );
        }
    }
}