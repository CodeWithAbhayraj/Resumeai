package com.example.resumeai.service;

import com.example.resumeai.ai.JDAgent;
import com.example.resumeai.dto.JDResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JDService {

    private final JDAgent jdAgent;

    public JDResponseDTO analyzeJobDescription(String jobDescription) {

        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException(
                    "Job description cannot be empty."
            );
        }

        return jdAgent.analyzeJD(jobDescription);
    }
}