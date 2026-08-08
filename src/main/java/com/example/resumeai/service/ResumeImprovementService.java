package com.example.resumeai.service;

import com.example.resumeai.ai.ResumeAgent;
import com.example.resumeai.ai.ResumeImprovementAgent;
import com.example.resumeai.dto.ATSResponseDTO;
import com.example.resumeai.dto.JDResponseDTO;
import com.example.resumeai.dto.ImprovedResumeDTO;
import com.example.resumeai.dto.ResumeResponseDTO;
import com.example.resumeai.util.PdfExtractor;
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

    public ImprovedResumeDTO improveResume(
            MultipartFile file,
            JDResponseDTO jobDescription
    ) {

        // ==============================
        // 1. PDF -> Resume Text
        // ==============================

        String resumeText = pdfExtractor.extractText(file);

        // ==============================
        // 2. Resume Analysis
        // ==============================

        ResumeResponseDTO resume =
                resumeAgent.analyzeResume(resumeText);

        // ==============================
        // 3. ATS Analysis
        // ==============================

        ATSResponseDTO atsResult =
                atsService.analyze(resume, jobDescription);

        // ==============================
        // 4. Resume Improvement
        // ==============================

        return resumeImprovementAgent.improveResume(
                resume,
                jobDescription,
                atsResult
        );
    }
}