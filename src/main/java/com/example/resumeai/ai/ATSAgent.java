package com.example.resumeai.ai;

import com.example.resumeai.dto.ATSResponseDTO;
import com.example.resumeai.dto.JDResponseDTO;
import com.example.resumeai.dto.ResumeResponseDTO;
import com.example.resumeai.prompt.ATSScorePrompt;
import com.example.resumeai.service.GeminiService;
import com.example.resumeai.service.JsonService;
import org.springframework.stereotype.Component;

@Component
public class ATSAgent {

    private final GeminiService geminiService;
    private final JsonService jsonService;

    public ATSAgent(GeminiService geminiService,
                    JsonService jsonService) {
        this.geminiService = geminiService;
        this.jsonService = jsonService;
    }

    public ATSResponseDTO analyzeATS(ResumeResponseDTO resume,
                                     JDResponseDTO jobDescription) {

        // DTO -> JSON
        String resumeJson = jsonService.toJson(resume);
        String jdJson = jsonService.toJson(jobDescription);

        // Build Prompt
        String prompt = String.format(
                ATSScorePrompt.PROMPT,
                resumeJson,
                jdJson
        );

        // Gemini Response
        String aiResponse = geminiService.askGemini(prompt);

        // JSON -> DTO
        return jsonService.fromJson(
                aiResponse,
                ATSResponseDTO.class
        );
    }
}