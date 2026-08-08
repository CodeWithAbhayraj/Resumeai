package com.example.resumeai.dto;

import lombok.Data;

import java.util.List;

@Data
public class ATSResponseDTO {

    // Overall ATS Score
    private int atsScore;

    // Skills present in both Resume & JD
    private List<String> matchedSkills;

    // Skills missing from Resume
    private List<String> missingSkills;

    // Strong points in resume
    private List<String> strengths;

    // Weak points in resume
    private List<String> weaknesses;

    // AI recommendations to improve resume
    private List<String> suggestions;

}