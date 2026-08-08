package com.example.resumeai.controller;

import com.example.resumeai.dto.ImprovedResumeDTO;
import com.example.resumeai.dto.JDResponseDTO;
import com.example.resumeai.service.ResumeImprovementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeImprovementController {

    private final ResumeImprovementService resumeImprovementService;
    private final ObjectMapper objectMapper;

    @PostMapping(
            value = "/improve",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ImprovedResumeDTO> improveResume(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam("jobDescription")
            String jobDescriptionJson

    ) throws Exception {

        // JSON String → JDResponseDTO
        JDResponseDTO jobDescription =
                objectMapper.readValue(
                        jobDescriptionJson,
                        JDResponseDTO.class
                );

        // Resume + JD → Improvement Agent
        ImprovedResumeDTO response =
                resumeImprovementService.improveResume(
                        file,
                        jobDescription
                );

        return ResponseEntity.ok(response);
    }
}