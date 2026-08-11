package com.example.resumeai.controller;

import com.example.resumeai.dto.ATSResponseDTO;
import com.example.resumeai.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ATSResponseDTO> uploadResume(

            @RequestParam("file") MultipartFile file,

            @RequestParam("jobDescription") String jobDescription

    ) {

        ATSResponseDTO response =
                resumeService.analyzeResume(
                        file,
                        jobDescription
                );

        return ResponseEntity.ok(response);
    }
}