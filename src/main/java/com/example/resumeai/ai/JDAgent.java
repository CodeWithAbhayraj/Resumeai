package com.example.resumeai.ai;

import com.example.resumeai.dto.JDResponseDTO;
import com.example.resumeai.prompt.JDPrompt;
import com.example.resumeai.service.GeminiService;
import com.example.resumeai.service.JsonService;
import org.springframework.stereotype.Component;

@Component
public class JDAgent {

    private final GeminiService geminiService;
    private final JsonService jsonService;

    public JDAgent(
            GeminiService geminiService,
            JsonService jsonService
    ) {
        this.geminiService = geminiService;
        this.jsonService = jsonService;
    }

    public JDResponseDTO analyzeJD(String jobDescription) {

        // JD text → Prompt
        String prompt = String.format(
                JDPrompt.PROMPT,
                jobDescription
        );

        // Prompt → Gemini
        String aiResponse = geminiService.askGemini(prompt);

        System.out.println("============= GEMINI JD RESPONSE =============");
        System.out.println(aiResponse);
        System.out.println("==============================================");

        // Gemini JSON → JDResponseDTO
        return jsonService.fromJson(
                aiResponse,
                JDResponseDTO.class
        );
    }
}