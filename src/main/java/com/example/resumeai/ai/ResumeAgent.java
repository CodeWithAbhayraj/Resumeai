package com.example.resumeai.ai;

import com.example.resumeai.dto.ResumeResponseDTO;
import com.example.resumeai.prompt.ResumePrompt;
import com.example.resumeai.service.GeminiService;
import com.example.resumeai.service.JsonService;
import org.springframework.stereotype.Component;

@Component
public class ResumeAgent {

    private final GeminiService geminiService;
    private final JsonService jsonService;

    public ResumeAgent(GeminiService geminiService,
                       JsonService jsonService) {
        this.geminiService = geminiService;
        this.jsonService = jsonService;
    }

    public ResumeResponseDTO analyzeResume(String resumeText) {

        String prompt = String.format(ResumePrompt.PROMPT, resumeText);

        String json = geminiService.askGemini(prompt);

        return jsonService.fromJson(json, ResumeResponseDTO.class);
    }

}