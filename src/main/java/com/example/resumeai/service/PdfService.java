package com.example.resumeai.service;

import com.example.resumeai.ai.PdfAgent;
import com.example.resumeai.dto.ImprovedResumeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final PdfAgent pdfAgent;

    public byte[] generateResumePdf(ImprovedResumeDTO resume) {

        return pdfAgent.generateResumePdf(resume);
    }
}