package com.example.resumeai.controller;

import com.example.resumeai.dto.JDResponseDTO;
import com.example.resumeai.service.JDService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jd")
@RequiredArgsConstructor
public class JDController {

    private final JDService jdService;

    @PostMapping("/analyze")
    public ResponseEntity<JDResponseDTO> analyzeJobDescription(
            @RequestBody String jobDescription
    ) {

        JDResponseDTO response =
                jdService.analyzeJobDescription(jobDescription);

        return ResponseEntity.ok(response);
    }
}