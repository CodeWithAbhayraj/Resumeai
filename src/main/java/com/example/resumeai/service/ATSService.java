package com.example.resumeai.service;

import com.example.resumeai.ai.ATSAgent;
import com.example.resumeai.dto.ATSResponseDTO;
import com.example.resumeai.dto.JDResponseDTO;
import com.example.resumeai.dto.ResumeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ATSService {

    private final ATSAgent atsAgent;

    public ATSResponseDTO analyze(
            ResumeResponseDTO resume,
            JDResponseDTO jobDescription
    ) {

        return atsAgent.analyzeATS(resume, jobDescription);

    }
}