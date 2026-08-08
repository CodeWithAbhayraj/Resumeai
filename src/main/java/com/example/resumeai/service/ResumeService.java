package com.example.resumeai.service;

import com.example.resumeai.ai.JDAgent;
import com.example.resumeai.ai.ResumeAgent;
import com.example.resumeai.dto.ATSResponseDTO;
import com.example.resumeai.dto.JDResponseDTO;
import com.example.resumeai.dto.ResumeResponseDTO;
import com.example.resumeai.util.PdfExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final PdfExtractor pdfExtractor;
    private final ResumeAgent resumeAgent;
    private final JDAgent jdAgent;
    private final ATSService atsService;

    public ATSResponseDTO analyzeResume(
            MultipartFile file,
            String jobDescription
    ) {

        // ==========================================
        // STEP 1: PDF -> Resume Text
        // ==========================================

        String resumeText = pdfExtractor.extractText(file);

        System.out.println("============= RESUME =============");
        System.out.println(resumeText);
        System.out.println("==================================");


        // ==========================================
        // STEP 2: Resume Text -> ResumeResponseDTO
        // ==========================================

        ResumeResponseDTO resume =
                resumeAgent.analyzeResume(resumeText);


        // ==========================================
        // STEP 3: Job Description -> JDResponseDTO
        // ==========================================

        JDResponseDTO jd =
                jdAgent.analyzeJD(jobDescription);


        System.out.println("============= JD =============");
        System.out.println(jd);
        System.out.println("==================================");


        // ==========================================
        // STEP 4: Resume + JD -> ATS Analysis
        // ==========================================

        return atsService.analyze(resume, jd);
    }
}