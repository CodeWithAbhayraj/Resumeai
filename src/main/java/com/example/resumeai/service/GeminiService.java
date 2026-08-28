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

            System.out.println("========== GEMINI ERROR ==========");
            System.out.println(message);
            System.out.println("===================================");

            // ==========================================
            // QUOTA / RATE LIMIT
            // ==========================================

            if (message != null &&
                    (
                            message.contains("429") ||
                                    message.contains("quota") ||
                                    message.contains("Quota") ||
                                    message.contains("RESOURCE_EXHAUSTED") ||
                                    message.contains("rate limit")
                    )) {

                throw new GeminiServiceException(
                        "AI server is currently busy or the usage limit has been reached. " +
                                "Please try again later."
                );
            }


            // ==========================================
            // GEMINI SERVER BUSY / HIGH DEMAND
            // ==========================================

            if (message != null &&
                    (
                            message.contains("503") ||
                                    message.contains("high demand") ||
                                    message.contains("temporarily unavailable") ||
                                    message.contains("UNAVAILABLE")
                    )) {

                throw new GeminiServiceException(
                        "AI server is currently busy due to high demand. " +
                                "Please try again later."
                );
            }


            // ==========================================
            // OTHER GEMINI ERROR
            // ==========================================

            throw new GeminiServiceException(
                    "AI service is temporarily unavailable. " +
                            "Please try again later."
            );
        }
    }
}