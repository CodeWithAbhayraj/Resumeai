package com.example.resumeai.controller;

import com.example.resumeai.service.GeminiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {

    private final GeminiService geminiService;

    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/test-ai")
    public String testAI() {

        return geminiService.askGemini("Say Hello from Gemini AI");

    }
}