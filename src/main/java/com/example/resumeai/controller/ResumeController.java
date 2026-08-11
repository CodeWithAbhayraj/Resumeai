package com.example.resumeai.controller;

import com.example.resumeai.ai.ResumeAgent;
import com.example.resumeai.dto.ATSResponseDTO;
import com.example.resumeai.dto.ResumeResponseDTO;
import com.example.resumeai.service.ResumeService;
import com.example.resumeai.util.PdfExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final PdfExtractor pdfExtractor;
    private final ResumeAgent resumeAgent;


    // ==========================================
    // ATS ANALYSIS
    // ==========================================

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ATSResponseDTO> uploadResume(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam("jobDescription")
            String jobDescription

    ) {

        ATSResponseDTO response =
                resumeService.analyzeResume(
                        file,
                        jobDescription
                );

        return ResponseEntity.ok(response);
    }


    // ==========================================
    // GENERATE / DOWNLOAD RESUME PDF
    // ==========================================

    @PostMapping(
            value = "/generate-pdf",
            consumes = "multipart/form-data",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> generatePdf(

            @RequestParam("file")
            MultipartFile file

    ) {

        // PDF -> Text
        String resumeText =
                pdfExtractor.extractText(file);

        // Text -> ResumeResponseDTO
        ResumeResponseDTO resume =
                resumeAgent.analyzeResume(resumeText);

        // ResumeResponseDTO -> PDF
        byte[] pdf =
                resumeService.generateResumePdf(resume);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Improved_Resume.pdf\""
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }
}