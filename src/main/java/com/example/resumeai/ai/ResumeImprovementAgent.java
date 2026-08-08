package com.example.resumeai.ai;

import com.example.resumeai.dto.ATSResponseDTO;
import com.example.resumeai.dto.JDResponseDTO;
import com.example.resumeai.dto.ImprovedResumeDTO;
import com.example.resumeai.dto.ResumeResponseDTO;
import com.example.resumeai.prompt.ResumeImprovementPrompt;
import com.example.resumeai.service.GeminiService;
import com.example.resumeai.service.JsonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResumeImprovementAgent {

    private final GeminiService geminiService;
    private final JsonService jsonService;

    public ImprovedResumeDTO improveResume(
            ResumeResponseDTO resume,
            JDResponseDTO jobDescription,
            ATSResponseDTO atsResult
    ) {

        // Resume -> JSON
        String resumeJson =
                jsonService.toJson(resume);

        // JD -> JSON
        String jdJson =
                jsonService.toJson(jobDescription);

        // ATS -> JSON
        String atsJson =
                jsonService.toJson(atsResult);

        // Build Prompt
        String prompt = String.format(
                ResumeImprovementPrompt.PROMPT,
                resumeJson,
                jdJson,
                atsJson
        );

        // Gemini
        String aiResponse =
                geminiService.askGemini(prompt);

        // JSON -> DTO
        return jsonService.fromJson(
                aiResponse,
                ImprovedResumeDTO.class
        );
    }
}