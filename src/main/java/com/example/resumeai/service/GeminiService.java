package com.example.resumeai.service;

import com.google.genai.Client;
import com.google.genai.errors.ServerException;
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

        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

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

                // Remove Markdown JSON wrapper
                result = result
                        .replace("```json", "")
                        .replace("```", "")
                        .trim();

                return result;

            } catch (ServerException e) {

                String message = e.getMessage();

                // Gemini server temporarily busy
                if (message != null && message.contains("503")) {

                    if (attempt < maxAttempts) {

                        System.out.println(
                                "Gemini API returned 503. " +
                                        "Retrying... Attempt " +
                                        (attempt + 1) +
                                        "/" + maxAttempts
                        );

                        try {
                            Thread.sleep(2000L * attempt);
                        } catch (InterruptedException interruptedException) {

                            Thread.currentThread().interrupt();

                            throw new RuntimeException(
                                    "Gemini retry interrupted.",
                                    interruptedException
                            );
                        }

                    } else {

                        throw new RuntimeException(
                                "Gemini API is temporarily unavailable " +
                                        "because the model is experiencing high demand. " +
                                        "Please try again later.",
                                e
                        );
                    }

                } else {

                    throw new RuntimeException(
                            "Gemini server error: " + message,
                            e
                    );
                }

            } catch (Exception e) {

                String message = e.getMessage();

                // Gemini API quota / rate limit
                if (message != null && message.contains("429")) {

                    throw new RuntimeException(
                            "Gemini API quota exceeded. " +
                                    "Please wait for quota reset or use a Gemini API project " +
                                    "with available quota.",
                            e
                    );
                }

                throw new RuntimeException(
                        "Error while calling Gemini API: " + message,
                        e
                );
            }
        }

        throw new RuntimeException(
                "Gemini API request failed after multiple attempts."
        );
    }
}