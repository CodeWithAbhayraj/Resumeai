package com.example.resumeai.service;

import com.example.resumeai.ai.ResumeAgent;
import com.example.resumeai.ai.ResumeImprovementAgent;
import com.example.resumeai.dto.ATSResponseDTO;
import com.example.resumeai.dto.JDResponseDTO;
import com.example.resumeai.dto.ImprovedResumeDTO;
import com.example.resumeai.dto.ResumeResponseDTO;
import com.example.resumeai.util.PdfExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ResumeImprovementService {

    private final PdfExtractor pdfExtractor;
    private final ResumeAgent resumeAgent;
    private final ATSService atsService;
    private final ResumeImprovementAgent resumeImprovementAgent;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public ImprovedResumeDTO improveResume(
            MultipartFile file,
            String jobDescriptionText
    ) {

        // ==============================
        // 1. Plain JD Text -> JD JSON
        // ==============================

        JDResponseDTO jobDescription =
                convertJDTextToJson(jobDescriptionText);

        // ==============================
        // 2. PDF -> Resume Text
        // ==============================

        String resumeText = pdfExtractor.extractText(file);

        // ==============================
        // 3. Resume Analysis
        // ==============================

        ResumeResponseDTO resume =
                resumeAgent.analyzeResume(resumeText);

        // ==============================
        // 4. ATS Analysis
        // ==============================

        ATSResponseDTO atsResult =
                atsService.analyze(resume, jobDescription);

        // ==============================
        // 5. Resume Improvement
        // ==============================

        return resumeImprovementAgent.improveResume(
                resume,
                jobDescription,
                atsResult
        );
    }

    private JDResponseDTO convertJDTextToJson(String jobDescriptionText) {

        String prompt = """
                You are a Job Description Analysis Agent.

                Convert the following plain-text Job Description into
                STRICT JSON.

                Return ONLY valid JSON.
                Do NOT return markdown.
                Do NOT return ```json.
                Do NOT add explanations before or after the JSON.

                The JSON must follow this exact structure:

                {
                  "jobTitle": "string",
                  "experienceRequired": "string",
                  "education": "string",
                  "requiredSkills": [],
                  "preferredSkills": []
                }

                Rules:
                - Extract the job title from the JD.
                - Extract required experience.
                - Extract education requirements.
                - Put mandatory/required technical skills in requiredSkills.
                - Put optional/preferred skills in preferredSkills.
                - If a value is not available, use an empty string.
                - If no skills are available, use an empty array.

                Job Description:
                %s
                """.formatted(jobDescriptionText);

        try {

            String jsonResponse =
                    geminiService.askGemini(prompt);

            return objectMapper.readValue(
                    jsonResponse,
                    JDResponseDTO.class
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to convert Job Description into JSON: "
                            + e.getMessage(),
                    e
            );
        }
    }
}