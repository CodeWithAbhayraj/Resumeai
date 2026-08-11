package com.example.resumeai.service;

import com.example.resumeai.ai.JDAgent;
import com.example.resumeai.ai.PdfAgent;
import com.example.resumeai.ai.ResumeAgent;
import com.example.resumeai.dto.ATSResponseDTO;
import com.example.resumeai.dto.JDResponseDTO;
import com.example.resumeai.dto.ResumeResponseDTO;
import com.example.resumeai.dto.ImprovedResumeDTO;
import com.example.resumeai.dto.ExperienceDTO;
import com.example.resumeai.util.PdfExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final PdfExtractor pdfExtractor;
    private final ResumeAgent resumeAgent;
    private final JDAgent jdAgent;
    private final ATSService atsService;
    private final PdfAgent pdfAgent;


    // ==========================================
    // STEP 1-4: RESUME + JD -> ATS ANALYSIS
    // ==========================================

    public ATSResponseDTO analyzeResume(
            MultipartFile file,
            String jobDescription
    ) {

        // ==========================================
        // STEP 1: PDF -> Resume Text
        // ==========================================

        String resumeText =
                pdfExtractor.extractText(file);

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


    // ==========================================
    // STEP 5: Generate Resume PDF
    // ==========================================

    public byte[] generateResumePdf(
            ResumeResponseDTO resume
    ) {

        ImprovedResumeDTO improvedResume =
                new ImprovedResumeDTO();


        // ==========================================
        // PERSONAL INFORMATION
        // ==========================================

        improvedResume.setFullName(
                resume.getFullName()
        );

        improvedResume.setEmail(
                resume.getEmail()
        );

        improvedResume.setGithub(
                resume.getGithub()
        );

        improvedResume.setLinkedin(
                resume.getLinkedin()
        );


        // ==========================================
        // PROFESSIONAL SUMMARY
        // ==========================================

        improvedResume.setProfessionalSummary(
                resume.getProfessionalSummary()
        );


        // ==========================================
        // SKILLS
        // ==========================================

        improvedResume.setSkills(
                resume.getSkills()
        );


        // ==========================================
        // PROJECTS
        // ==========================================

        improvedResume.setProjects(
                resume.getProjects()
        );


        // ==========================================
        // EDUCATION
        // ==========================================

        improvedResume.setEducation(
                resume.getEducation()
        );


        // ==========================================
        // CERTIFICATIONS
        // ==========================================

        improvedResume.setCertifications(
                resume.getCertifications()
        );


        // ==========================================
        // EXPERIENCE
        // ==========================================

        if (resume.getExperience() != null) {

            List<ExperienceDTO> experienceList =
                    resume.getExperience()
                            .stream()
                            .map(exp -> {

                                ExperienceDTO dto =
                                        new ExperienceDTO();

                                dto.setDescription(exp);

                                return dto;
                            })
                            .toList();

            improvedResume.setExperience(
                    experienceList
            );
        }


        // ==========================================
        // PDF GENERATION
        // ==========================================

        return pdfAgent.generateResumePdf(
                improvedResume
        );
    }
}