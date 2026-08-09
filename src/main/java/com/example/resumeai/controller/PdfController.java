package com.example.resumeai.controller;

import com.example.resumeai.dto.ImprovedResumeDTO;
import com.example.resumeai.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    @PostMapping(
            value = "/pdf",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> generatePdf(
            @RequestBody ImprovedResumeDTO resume
    ) {

        byte[] pdf =
                pdfService.generateResumePdf(resume);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ATS-Resume.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}